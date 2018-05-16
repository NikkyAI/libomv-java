/**
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
 * - Neither the name of the libomv-java project nor the names of its
 *   contributors may be used to endorse or promote products derived from
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
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import libomv.utils.Helpers;

// This class holds the GUI resource locations
public final class Resources {
	private static final Logger logger = Logger.getLogger(Resources.class);
	// The 16x16 application icon
	public static final String ICON_APPLICATION = "images/app-logo.png";

	// The 16x16 online icon for displaying next to a name in lists
	public static final String ICON_ONLINE = "images/user-online.png";
	// The 16x16 offline icon for displaying next to a name in lists
	public static final String ICON_OFFLINE = "images/user-offline.png";
	// The 16x16 can see online status icon for displaying next to a name in friends
	// list
	public static final String ICON_VISIBLE_ONLINE = "images/ff_visible_online.png";
	// The 16x16 can map status icon for displaying next to a name in friends list
	public static final String ICON_VISIBLE_MAP = "images/ff_visible_map.png";
	// The 16x16 can edit mine status icon for displaying next to a name in friends
	// list
	public static final String ICON_EDIT_MINE = "images/ff_edit_mine.png";
	// The 16x16 can edit theirs status icon for displaying next to a name in
	// friends list
	public static final String ICON_EDIT_THEIRS = "images/ff_edit_theirs.png";
	// The 16x16 close tab icon for displaying next to a closable tab name
	public static final String ICON_CLOSE_TAB = "images/";
	// The 16x16 typing icon for displaying next to an agent's name when they are
	// typing
	public static final String ICON_TYPING = "images/avatar_typing.png";
	// The 16x16 "message pending" icon for displaying next to an agent's name
	// when there are pending messages
	public static final String ICON_PENDING_MESSAGES = "images/mail-message-new.png";
	// The 16x16 "unknown status" icon
	public static final String ICON_UNKNOWN_STATUS = "images/";

	public static final String INV_FOLDER_ANIMATION = "images/inv_folder_animation.png";
	public static final String INV_FOLDER_BODYPART = "images/inv_folder_bodypart.png";
	public static final String INV_FOLDER_CALLINGCARD = "images/inv_folder_callingcard.png";
	public static final String INV_FOLDER_CLOTHING = "images/inv_folder_clothing.png";
	public static final String INV_FOLDER_GESTURE = "images/inv_folder_gesture.png";
	public static final String INV_FOLDER_LANDMARK = "images/inv_folder_landmark.png";
	public static final String INV_FOLDER_LOSTANDFOUND = "images/inv_folder_lostandfound.png";
	public static final String INV_FOLDER_NOTECARD = "images/inv_folder_notecard.png";
	public static final String INV_FOLDER_OBJECT = "images/inv_folder_object.png";
	public static final String INV_FOLDER_PLAIN_CLOSED = "images/inv_folder_plain_closed.png";
	public static final String INV_FOLDER_PLAIN_OPEN = "images/inv_folder_plain_open.png";
	public static final String INV_FOLDER_SCRIPT = "images/inv_folder_script.png";
	public static final String INV_FOLDER_SNAPSHOT = "images/inv_folder_snapshot.png";
	public static final String INV_FOLDER_SOUND = "images/inv_folder_sound.png";
	public static final String INV_FOLDER_TEXTURE = "images/inv_folder_texture.png";
	public static final String INV_FOLDER_TRASH = "images/inv_folder_trash.png";
	public static final String INV_ITEM_ALPHA = "images/inv_item_alpha.png";
	public static final String INV_ITEM_ANIMATION = "images/inv_item_animation.png";
	public static final String INV_ITEM_ATTACH = "images/inv_item_attach.png";
	public static final String INV_ITEM_CALLINGCARD_OFFLINE = "images/inv_item_callingcard_offline.png";
	public static final String INV_ITEM_CALLINGCARD_ONLINE = "images/inv_item_callingcard_online.png";
	public static final String INV_ITEM_CLOTHING = "images/inv_item_cothing.png";
	public static final String INV_ITEM_EYES = "images/inv_item_eyes.png";
	public static final String INV_ITEM_GESTURE = "images/inv_item_gesture.png";
	public static final String INV_ITEM_GLOVES = "images/inv_item_gloves.png";
	public static final String INV_ITEM_HAIR = "images/inv_item_hair.png";
	public static final String INV_ITEM_JACKET = "images/inv_item_jacket.png";
	public static final String INV_ITEM_LANDMARK = "images/inv_item_landmark.png";
	public static final String INV_ITEM_LANDMARK_VISITED = "images/inv_item_landmark_visited.png";
	public static final String INV_ITEM_NOTECARD = "images/inv_item_notecard.png";
	public static final String INV_ITEM_OBJECT = "images/inv_item_object.png";
	public static final String INV_ITEM_OBJECT_MULTI = "images/inv_item_object_multi.png";
	public static final String INV_ITEM_PANT = "images/inv_item_pant.png";
	public static final String INV_ITEM_PHYSICS = "images/inv_item_physics.png";
	public static final String INV_ITEM_SCRIPT = "images/inv_item_script.png";
	public static final String INV_ITEM_SCRIPT_DANGEROUS = "images/inv_item_script_dangerous.png";
	public static final String INV_ITEM_SHAPE = "images/inv_item_shape.png";
	public static final String INV_ITEM_SHIRT = "images/inv_item_shirt.png";
	public static final String INV_ITEM_SHOES = "images/inv_item_shoes.png";
	public static final String INV_ITEM_SKIN = "images/inv_item_skin.png";
	public static final String INV_ITEM_SKIRT = "images/inv_item_skirt.png";
	public static final String INV_ITEM_SNAPSHOT = "images/inv_item_snapshot.png";
	public static final String INV_ITEM_SOCKS = "images/inv_item_socks.png";
	public static final String INV_ITEM_SOUND = "images/inv_item_sound.png";
	public static final String INV_ITEM_TATTOO = "images/inv_item_tattoo.png";
	public static final String INV_ITEM_TEXTURE = "images/inv_item_texture.png";
	public static final String INV_ITEM_UNDERPANTS = "images/inv_item_underpants.png";
	public static final String INV_ITEM_UNDERSHIRT = "images/inv_item_undershirt.png";

	// The exit button icon
	public static final String ICON_BUTTON_EXIT = "images/";
	// The logout button icon
	public static final String ICON_BUTTON_LOGOUT = "images/";
	// The login button icon
	public static final String ICON_BUTTON_LOGIN = "images/";

	public static final String ICON_BUTTON_CURRENCY = "images/status_buy_currency.png";

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

	public static BufferedImage loadImage(String location) {
		InputStream is = Resources.class.getClassLoader().getResourceAsStream(location);
		try {
			return ImageIO.read(is);
		} catch (IOException ex) {
			logger.debug("Couldn't load image resource " + location, ex);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		return null;
	}

	public static ImageIcon loadIcon(String location) {
		return loadIcon(location, location);
	}

	public static ImageIcon loadIcon(String location, String name) {
		InputStream is = Resources.class.getClassLoader().getResourceAsStream(location);
		try {
			return new ImageIcon(ImageIO.read(is), name);
		} catch (IOException ex) {
			logger.debug("Couldn't load icon resource " + name, ex);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		return null;
	}

	public static String loadTextFile(String location) {
		InputStream is = Resources.class.getClassLoader().getResourceAsStream(location);
		try {
			return loadTextFile(is);
		} catch (SecurityException ex) {
			logger.debug("Couldn't load resource " + location, ex);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		return Helpers.EmptyString;
	}

	private static String loadTextFile(InputStream is) {
		String retval = Helpers.EmptyString;
		Scanner scanner = new Scanner(is, Helpers.UTF8_ENCODING);
		scanner.useDelimiter("\\A");
		if (scanner.hasNext())
			retval = scanner.next();
		scanner.close();
		return retval;
	}
}
