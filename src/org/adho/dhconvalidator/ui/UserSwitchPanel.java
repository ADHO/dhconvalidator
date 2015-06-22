/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

import java.util.List;

import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.user.User;
import org.adho.dhconvalidator.user.UserList;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.BaseTheme;

/**
 * A panel that provides the UI for switching to another user.
 * 
 * @author marco.petris@web.de
 *
 */
public class UserSwitchPanel extends HorizontalLayout {
	
	private ComboBox userSwitchBox;
	private Button btReload;

	public UserSwitchPanel() {
		initComponents();
		initActions();
	}

	/**
	 * Setup behaviour.
	 */
	private void initActions() {
		btReload.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				// reload the user list and refill box
				UserList.INSTANCE.reload();
				User selected = (User) userSwitchBox.getValue();
				userSwitchBox.removeAllItems();
				List<User> users = UserList.INSTANCE.getUsers();
				setUsers(users);
				if (selected != null && users.contains(selected)) {
					userSwitchBox.setValue(selected);
				}
			}
		});
		
		userSwitchBox.addValueChangeListener(new ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				
				// switch to selected user
				
				User oldUser = 
					(User) VaadinSession.getCurrent().getAttribute(
							SessionStorageKey.USER.name());
				if (oldUser.isAdmin()) {
					User selectedUser = (User) event.getProperty().getValue();
					if ((selectedUser != null) && (!oldUser.equals(selectedUser))) {
						VaadinSession.getCurrent().setAttribute(
								SessionStorageKey.USER.name(), 
								new User(
									selectedUser.getUserId(), 
									selectedUser.getFirstName(), 
									selectedUser.getLastName(), 
									selectedUser.getEmail(),
									true));
						Notification.show(
							Messages.getString("UserSwitchPanel.notificationTitle"), //$NON-NLS-1$
							Messages.getString("UserSwitchPanel.notificationContent", //$NON-NLS-1$
									selectedUser.getLastName(), 
									selectedUser.getFirstName(),
									selectedUser.getEmail()), 
							Type.HUMANIZED_MESSAGE);
					}
				}
				else {
					throw new IllegalStateException(
							Messages.getString("UserSwitchPanel.illegalState")); //$NON-NLS-1$
				}
			}
		});
	}

	/**
	 * Fill box with given users.
	 * @param users
	 */
	private void setUsers(List<User> users) {
		userSwitchBox.addItems(users);
		for (User user : users) {
			userSwitchBox.setItemCaption(
				user, 
				user.getLastName() + ", " 
					+ user.getFirstName() + ", " 
					+ user.getEmail() 
					+ " (#" + user.getUserId() + ")");
		}
	}

	/**
	 * Setup UI.
	 */
	private void initComponents() {
		List<User> users = UserList.INSTANCE.getUsers();
		userSwitchBox = new ComboBox(Messages.getString("UserSwitchPanel.boxCaption")); //$NON-NLS-1$
		setUsers(users);
		User current = (User) VaadinSession.getCurrent().getAttribute(SessionStorageKey.USER.name());
		userSwitchBox.setValue(current);
		
		userSwitchBox.setDescription(
			Messages.getString("UserSwitchPanel.boxDescription")); //$NON-NLS-1$
		userSwitchBox.setNewItemsAllowed(false);
		userSwitchBox.setNullSelectionAllowed(false);
		
		addComponent(userSwitchBox);
		btReload = new Button(Messages.getString("UserSwitchPanel.reloadCaption")); //$NON-NLS-1$
		btReload.setStyleName(BaseTheme.BUTTON_LINK);
		btReload.addStyleName("plain-link"); //$NON-NLS-1$
		
		addComponent(btReload);
	}

}
