/**
 * Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names
 *   of its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
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
package libomv.Gui;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.imageio.ImageIO;

import libomv.utils.Helpers;
import libomv.utils.Logger;
import libomv.utils.Logger.LogLevel;

// This class holds the GUI resource locations
public final class Resources
{
	// The 16x16 application icon
	public static final String ICON_APPLICATION = "images/app-logo.png";

	// The 16x16 online icon for displaying next to a name in lists
	public static final String ICON_ONLINE = "images/user-online.png";
	// The 16x16 offline icon for displaying next to a name in lists
	public static final String ICON_OFFLINE = "images/user-offline.png";
	// The 16x16 close tab icon for displaying next to a closable tab name
	public static final String ICON_CLOSE_TAB = "images/";
	// The 16x16 typing icon for displaying next to an agent's name when they
	// are typing
	public static final String ICON_TYPING = "images/";
	// The 16x16 "message pending" icon for displaying next to an agent's name
	// when there are pending messages
	public static final String ICON_PENDING_MESSAGES = "images/mail-message-new.png";
	// The 16x16 "unknown status" icon
	public static final String ICON_UNKNOWN_STATUS = "images/";

	// The exit button icon
	public static final String ICON_BUTTON_EXIT = "images/";
	// The logout button icon
	public static final String ICON_BUTTON_LOGOUT = "images/";
	// The login button icon
	public static final String ICON_BUTTON_LOGIN = "images/";

	// The 32x32 alert image
	public static final String IMAGE_ALERT = "images/dialog-warning.png";
	// The 32x32 error image
	public static final String IMAGE_ERROR = "images/dialog-error.png";
	// The 32x32 informational image
	public static final String IMAGE_INFORMATIONAL = "images/dialog-information.png";
	// The 32x32 question image
	public static final String IMAGE_QUESTION = "images/dialog-question.png";

	// The image missing image
	public static final String IMAGE_MISSING = "images/image-missing.png";
	// The image loading image 178x133 (for Second Life profile pictures)
	public static final String IMAGE_LOADING = "images/image-loading.png";

	// The image to show for busy status
	public static final String IMAGE_LOGO = "images/logo.png";
	// The image to show for busy status
	public static final String IMAGE_WORKING = "images/process-working.png";

	public static BufferedImage loadImage(String name)
	{
		InputStream is = Resources.class.getClassLoader().getResourceAsStream("res/" + name);
		try
		{
			return ImageIO.read(is);
		}
		catch (IOException ex)
		{
			Logger.Log("Couldn't load resource " + name, LogLevel.Debug, ex);
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (IOException e) { }
		}
		return null;
	}
	
	public static String loadTextFile(String name)
	{
		InputStream is = Resources.class.getClassLoader().getResourceAsStream("res/" + name);
		try
		{
			return loadTextFile(is);
		}
		catch (SecurityException ex)
		{
			Logger.Log("Couldn't load resource " + name, LogLevel.Debug, ex);
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (IOException e) { }
		}
		return Helpers.EmptyString;
	}
	
	private static String loadTextFile(InputStream is)
	{
		Scanner scanner = new Scanner(is, Helpers.UTF8_ENCODING).useDelimiter("\\A");
		if (scanner.hasNext())
			return scanner.next();
		return Helpers.EmptyString;
	}
}
