/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv.io.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.nio.reactor.IOReactorException;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSD.OSDFormat;
import libomv.StructuredData.OSD.OSDType;
import libomv.io.capabilities.CapsClient;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.types.UUID;
import libomv.types.Vector3;
import libomv.utils.Helpers;

public class RegistrationApi {
	static final int REQUEST_TIMEOUT = 1000 * 100;

	private class UserInfo {
		public String firstName;
		public String lastName;
		public String password;
	}

	private class RegistrationCaps {
		public URI createUser;
		public URI checkName;
		public URI getLastNames;
		public URI getErrorCodes;
	}

	public class ErrorCode {
		public int code;
		public String name;
		public String description;

		public ErrorCode(int code, String name, String description) {
			this.code = code;
			this.name = name;
			this.description = description;
		}

		@Override
		public String toString() {
			return String.format("Code: %d, Name: %s, Description: %s", code, name, description);
		}
	}

	// See
	// https://secure-web6.secondlife.com/developers/third_party_reg/#service_create_user
	// or
	// https://wiki.secondlife.com/wiki/RegAPIDoc for description
	public class CreateUserParam {
		public String firstName;
		public int lastNameID;
		public String email;
		public String password;
		public Date birthdate;

		// optional:
		public Integer limitedToEstate;
		public String startRegionName;
		public Vector3 startLocation;
		public Vector3 startLookAt;
	}

	private UserInfo userInfo;
	private RegistrationCaps caps;
	private int initializing;
	private Map<Integer, ErrorCode> errors;
	private Map<String, Integer> lastNames;

	public boolean getInitializing() {
		return (initializing < 0);
	}

	public RegistrationApi(String firstName, String lastName, String password)
			throws IOReactorException, UnsupportedEncodingException, URISyntaxException, InterruptedException,
			ExecutionException, TimeoutException {
		initializing = -2;

		userInfo = new UserInfo();

		userInfo.firstName = firstName;
		userInfo.lastName = lastName;
		userInfo.password = password;

		getCapabilities();
	}

	public void waitForInitialization() throws InterruptedException {
		while (getInitializing())
			Thread.sleep(10);
	}

	private URI getRegistrationApiCaps() throws URISyntaxException {
		return new URI("https://cap.secondlife.com/get_reg_capabilities");
	}

	private void getCapabilities() throws URISyntaxException, IOReactorException, UnsupportedEncodingException,
			InterruptedException, ExecutionException, TimeoutException {
		// build post data
		String postData = String.format("first_name=%s&last_name=%s&password=%s", userInfo.firstName, userInfo.lastName,
				userInfo.password);

		Future<OSD> future = new CapsClient(null, "get_reg_capabilities").executeHttpPost(getRegistrationApiCaps(),
				postData, "application/x-www-form-urlencoded", Helpers.UTF8_ENCODING);
		OSD response = future.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		if (response instanceof OSDMap) {
			OSDMap respTable = (OSDMap) response;
			// parse
			caps = new RegistrationCaps();

			caps.createUser = respTable.get("create_user").asUri();
			caps.checkName = respTable.get("check_name").asUri();
			caps.getLastNames = respTable.get("get_last_names").asUri();
			caps.getErrorCodes = respTable.get("get_error_codes").asUri();

			// finalize
			initializing++;

			errors = getErrorCodes(caps.getErrorCodes);
		}
	}

	/**
	 * Retrieves a list of error codes, and their meaning, that the RegAPI can
	 * return.
	 *
	 * @param capability
	 *            the capability URL for the "get_error_codes" RegAPI function.
	 * @return a mapping from error codes (as a number) to an ErrorCode object which
	 *         contains more detail on that error code.
	 */
	private Map<Integer, ErrorCode> getErrorCodes(URI capability)
			throws IOReactorException, InterruptedException, ExecutionException, TimeoutException {
		final Map<Integer, ErrorCode> errorCodes = new HashMap<>();

		Future<OSD> future = new CapsClient(null, "getErrorCodes").executeHttpGet(capability);
		OSD response = future.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		if (response instanceof OSDArray) {
			OSDArray respTable = (OSDArray) response;

			for (Iterator<OSD> iter = respTable.iterator(); iter.hasNext();) {
				OSDArray errors = (OSDArray) iter.next();

				errorCodes.put(errors.get(0).asInteger(),
						new ErrorCode(errors.get(0).asInteger(), errors.get(1).asString(), errors.get(2).asString()));
			}

			// finalize
			initializing++;
		}
		return errorCodes;
	}

	/**
	 * Retrieves a list of valid last names for newly created accounts.
	 *
	 * @param capability
	 *            the capability URL for the "get_last_names" RegAPI function.
	 * @return a mapping from last names, to their ID (needed for createUser()).
	 */
	private Map<String, Integer> getLastNames(URI capability)
			throws IOReactorException, InterruptedException, ExecutionException, TimeoutException {
		final SortedMap<String, Integer> lastNames = new TreeMap<>();

		Future<OSD> future = new CapsClient(null, "getLastNames").executeHttpGet(capability);
		OSD response = future.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		if (response instanceof OSDMap) {
			OSDMap respTable = (OSDMap) response;

			for (Entry<String, OSD> entry : respTable.entrySet()) {
				lastNames.put(entry.getValue().asString(), Integer.valueOf(entry.getKey()));
			}
		}
		return lastNames;
	}

	/**
	 * Retrieves a list of valid last names for newly created accounts.
	 *
	 * @return a mapping from last names, to their ID (needed for createUser()).
	 */
	public synchronized Map<String, Integer> getLastNames()
			throws IOReactorException, InterruptedException, ExecutionException, TimeoutException {
		if (lastNames.size() <= 0) {
			if (getInitializing())
				throw new IllegalStateException("still initializing");

			if (caps.getLastNames == null)
				throw new UnsupportedOperationException(
						"access denied: only approved developers have access to the registration api");

			lastNames = getLastNames(caps.getLastNames);
		}
		return lastNames;
	}

	/**
	 * Checks whether a name is already used in Second Life.
	 *
	 * @param firstName
	 *            of the name to check.
	 * @param lastNameID
	 *            the ID (see getLastNames() for the list of valid last name IDs) to
	 *            check.
	 * @return true if they already exist, false if the name is available.
	 * @throws Exception
	 */
	public boolean checkName(String firstName, int lastNameID) throws Exception {
		if (getInitializing())
			throw new IllegalStateException("still initializing");

		if (caps.checkName == null)
			throw new UnsupportedOperationException(
					"access denied; only approved developers have access to the registration api");

		// Create the POST data
		OSDMap query = new OSDMap();
		query.put("username", OSD.fromString(firstName));
		query.put("last_name_id", OSD.fromInteger(lastNameID));

		Future<OSD> future = new CapsClient(null, "checkName").executeHttpPost(caps.getLastNames, query, OSDFormat.Xml);
		OSD response = future.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		if (response.getType() != OSDType.Boolean)
			throw new Exception("check_name did not return a boolean as the only element inside the <llsd> tag.");
		return response.asBoolean();
	}

	/**
	 * Returns the new user ID or throws an exception containing the error code The
	 * error codes can be found here: https://wiki.secondlife.com/wiki/RegAPIError
	 *
	 * @param user
	 *            New user account to create
	 * @returns The UUID of the new user account
	 * @throws Exception
	 */
	public UUID createUser(CreateUserParam user) throws Exception {
		if (getInitializing())
			throw new IllegalStateException("still initializing");

		if (caps.createUser == null)
			throw new UnsupportedOperationException(
					"access denied; only approved developers have access to the registration api");

		// Create the POST data
		OSDMap query = new OSDMap();
		query.put("username", OSD.fromString(user.firstName));
		query.put("last_name_id", OSD.fromInteger(user.lastNameID));
		query.put("email", OSD.fromString(user.email));
		query.put("password", OSD.fromString(user.password));
		query.put("dob", OSD.fromString(new SimpleDateFormat("yyyy-MM-dd").format(user.birthdate)));

		if (user.limitedToEstate != null)
			query.put("limited_to_estate", OSD.fromInteger(user.limitedToEstate));

		if (user.startRegionName != null && !user.startRegionName.isEmpty())
			query.put("start_region_name", OSD.fromString(user.startRegionName));

		if (user.startLocation != null) {
			query.put("start_local_x", OSD.fromReal(user.startLocation.x));
			query.put("start_local_y", OSD.fromReal(user.startLocation.y));
			query.put("start_local_z", OSD.fromReal(user.startLocation.z));
		}

		if (user.startLookAt != null) {
			query.put("start_look_at_x", OSD.fromReal(user.startLookAt.x));
			query.put("start_look_at_y", OSD.fromReal(user.startLookAt.y));
			query.put("start_look_at_z", OSD.fromReal(user.startLookAt.z));
		}

		// Make the request
		Future<OSD> future = new CapsClient(null, "createUser").executeHttpPost(caps.createUser, query, OSDFormat.Xml);
		OSD response = future.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
		if (response instanceof OSDMap) {
			OSDMap map = (OSDMap) response;
			return map.get("agent_id").asUUID();
		}

		// an error happened
		OSDArray al = (OSDArray) response;

		StringBuilder sb = new StringBuilder();

		for (OSD ec : al) {
			if (sb.length() > 0)
				sb.append("; ");

			sb.append(errors.get(ec.asInteger()));
		}
		throw new Exception("failed to create user: " + sb.toString());
	}
}