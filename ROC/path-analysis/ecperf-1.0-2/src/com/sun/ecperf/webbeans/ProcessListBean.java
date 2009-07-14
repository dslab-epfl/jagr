
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: ProcessListBean.java,v 1.1.1.1 2002/11/16 05:35:31 emrek Exp $
 *
 *
 */
package com.sun.ecperf.webbeans;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject;

import java.io.Serializable;

import com.sun.ecperf.orders.itement.ejb.*;
import com.sun.ecperf.common.*;

import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * This is a helper bean used by jsp files to get all the items
 * that can be ordered. This bean is also used to hold the items
 * in the user list. As a result it performs adding and removing
 * items from the user list.
 *
 * @author Venkata Yella - yella@eng.sun.com
 *
 */
public class ProcessListBean implements Serializable {

	Vector                      items_list;
	Vector                      customer_list;
	Vector                      customer_list_prev;
	String                      customer_id;
	String                      order_number;
	Vector                      item_name_id_list;
	protected Debug             debug;
	protected boolean	        debugging;
	ItemEntHome                 itement_home;
	private static final String jndiname = "java:comp/env/ejb/ItemEnt";

	/**
	 * Constructor ProcessListBean
	 *
	 *
	 * @throws OtherException
	 *
	 */
	public ProcessListBean() throws OtherException {

		customer_list      = new Vector();
		customer_list_prev = new Vector();

		try {
			Context context    = new InitialContext();
			int     debugLevel = 0;

/*			try {
				debugLevel =
					((Integer) context.lookup("java:comp/env/debuglevel"))
						.intValue();
			} catch (Exception e) {

				// If there's an error looking up debuglevel,
				// just leave it as the default - 0
			}
*/			if (debugLevel > 0) {
				debug = new DebugPrint(debugLevel, this);
				debugging = true;
			} else {
				debug = new Debug();
				debugging = false;
			}
			if (debugging) 
				debug.println(3, "In constructor of ProcessListBean");

			Object obj = context.lookup(jndiname);

			if (debugging) 
				debug.println(3, "Looked up " + jndiname);

			itement_home = (ItemEntHome) PortableRemoteObject.narrow(obj,
					ItemEntHome.class);
		} catch (NamingException e) {
			throw new OtherException(
				"Naming Exception occured for the request.", e);
		}
	}

	/*
	 * This method is called the very first time. After that the getList method
	 * is called.
	 */

	/**
	 * Method getItemsList - Get a list of all items
	 *
	 *
	 * @return Vector - String list of all items
	 *
	 * @throws OtherException
	 *
	 */
	public Vector getItemsList() throws OtherException {
		
		if (items_list == null) {
			try {
				ItemEnt     itement;
				Enumeration items;

				if (debugging) 
					debug.println(3, "Find all ItemEnt beans");

				items             = itement_home.findAll();
				items_list        = new Vector();
				item_name_id_list = new Vector();
				while (items.hasMoreElements()) {
					itement =
						(ItemEnt) javax.rmi.PortableRemoteObject
							.narrow(items.nextElement(), ItemEnt.class);

					if (debugging) 
						debug.println(3, "Item Name and price are "
								  + itement.getName() + " "
								  + itement.getPrice());

					items_list.add(itement.getId() + "(Price - " + itement.getPrice()+")");
//					items_list.add(itement.getName() + "(Price - " + itement.getPrice()+")");
					item_name_id_list.add(new ItemNameId(itement.getId(), itement.getId()));
//					item_name_id_list.add(new ItemNameId(itement.getName(), itement.getId()));
					if (debugging) 
					       debug.println(3, "Item ID and price are " + itement.getId() + " " + itement.getPrice()); 
				}

				return items_list;
			} catch (ClassCastException e) {
				throw new OtherException(
					" ClassCast Exception occured for the request.", e);
			} catch (RemoteException e) {
				throw new OtherException(
					" Remote Exception occured for the request.", e);
			} catch (FinderException e) {
				throw new OtherException(
					" Finder Exception occured for the request.", e);
			} catch (Exception e) {
				throw new OtherException(
					" Unknown Exception occured for the request.", e);
			}
		} else {
			return items_list;
		}
	}

	/**
	 * Method addItem - Add an item to the current list
	 *
	 *
	 * @param item_str Item selected
	 * @param quantity Quantity to be added
	 *
	 * @throws OtherException
	 *
	 */
	public void addItem(String item_str, String quantity)
			throws OtherException {

		String       item_name, item_id, new_item;
		int          qty, cur_qty, new_qty;
		CustomerItem cust_item;
		ItemNameId   item_name_id;

		if (debugging) 
			debug.println(3, "In add Item of ProcessListBean ");

		try {
			qty = Integer.parseInt(quantity);

			if (qty <= 0) {
				throw new OtherException(" Quantity should be positive");
			}
		} catch (NumberFormatException e) {
			throw new OtherException(" Quantity should be an integer value ");
		}

		item_id   = null;
		item_name = item_str.substring(0, item_str.indexOf("(Price -"));

		for (int j = 0; j < item_name_id_list.size(); j++) {
			item_name_id = (ItemNameId) item_name_id_list.elementAt(j);

			if (item_name_id.item_name.equals(item_name)) {
				item_id = item_name_id.item_id;

				break;
			}
		}

		if (item_id == null) {
			throw new OtherException(
				" Item Id for the Item Name to be added is not found in the list. Something wrong in the code ");
		} else {
			for (int i = 0; i < customer_list.size(); i++) {
				cust_item = (CustomerItem) customer_list.elementAt(i);

				if (cust_item.item_id.equals(item_id)) {
					cur_qty   = cust_item.qty;
					new_qty   = cur_qty + qty;
					cust_item = new CustomerItem(item_name, item_id, new_qty);

					customer_list.setElementAt(cust_item, i);

					return;
				}
			}

			cust_item = new CustomerItem(item_name, item_id, qty);

			customer_list.add(cust_item);
		}
	}

	/**
	 * Method removeItem - Remove an item from the current list
	 *
	 *
	 * @param item_str Item selected
	 * @param quantity Quantity to be removed
	 *
	 * @throws OtherException
	 *
	 */
	public void removeItem(String item_str, String quantity)
			throws OtherException {

		String       item_name, item_id, new_item;
		int          qty, cur_qty, new_qty;
		CustomerItem cust_item;
		ItemNameId   item_name_id;

		if (debugging) 
			debug.println(3, "In remove Item of ProcessListBean ");

		try {
			qty = Integer.parseInt(quantity);

			if (qty <= 0) {
				throw new OtherException(" Quantity should be positive");
			}
		} catch (NumberFormatException e) {
			throw new OtherException(" Quantity should be an integer value ");
		}

		item_id   = null;
		item_name = item_str.substring(0, item_str.indexOf("(Price -"));

		for (int j = 0; j < item_name_id_list.size(); j++) {
			item_name_id = (ItemNameId) item_name_id_list.elementAt(j);

			if (item_name_id.item_name.equals(item_name)) {
				item_id = item_name_id.item_id;

				break;
			}
		}

		if (item_id == null) {
			throw new OtherException(
				" Item Id for the Item Name to be removed is not found in the list. Something wrong in the code ");
		} else {
			for (int i = 0; i < customer_list.size(); i++) {
				cust_item = (CustomerItem) customer_list.elementAt(i);

				if (cust_item.item_id.equals(item_id)) {
					cur_qty = cust_item.qty;

					if (qty >= cur_qty) {
						customer_list.removeElementAt(i);
					} else {
						new_qty   = cur_qty - qty;
						cust_item = new CustomerItem(item_name, item_id,
													 new_qty);

						customer_list.setElementAt(cust_item, i);
					}

					return;
				}
			}
		}
	}

	/**
	 * Method setCustomerList - Set customer list the list passed
	 *
	 *
	 * @param cust_status List of customer items
	 *
	 * @throws OtherException
	 *
	 */
	public void setCustomerList(CustomerOrderStatus cust_status)
			throws OtherException {

		String     item_name;
		String     item_id;
		int        qty;
		ItemNameId item_name_id;

		customer_list.removeAllElements();
		customer_list_prev.removeAllElements();

		if (item_name_id_list == null) {
			this.getItemsList();
		}

		for (int i = 0; i < cust_status.cust_items.length; i++) {
			item_name = null;
			item_id   = cust_status.cust_items[i].item_id;
			qty       = cust_status.cust_items[i].qty;

			for (int j = 0; j < item_name_id_list.size(); j++) {
				item_name_id = (ItemNameId) item_name_id_list.elementAt(j);

				if (item_name_id.item_id.equals(item_id)) {
					item_name = item_name_id.item_name;

					break;
				}
			}

			if (item_name == null) {
				throw new OtherException(
					" Item Name not found for Item Id " + item_id
					+ " in the item_name_id_list. Please check the code ");
			} else {
				customer_list.add(new CustomerItem(item_name, item_id, qty));
				customer_list_prev.add(new CustomerItem(item_name, item_id,
														qty));
			}
		}
	}

	/**
	 * Method addRemovedItemsToCustomerList
	 *
	 *
	 */
	public void addRemovedItemsToCustomerList() {

		CustomerItem cust_prev;
		boolean      item_found;

		for (int i = 0; i < customer_list_prev.size(); i++) {
			cust_prev  = (CustomerItem) customer_list_prev.elementAt(i);
			item_found = false;

			for (int j = 0; j < customer_list.size(); j++) {
				if (cust_prev.item_id
						.equals(((CustomerItem) customer_list.elementAt(j))
							.item_id)) {
					item_found = true;

					break;
				}
			}

			if (!item_found) {
				customer_list.add(new CustomerItem(cust_prev.item_name,
												   cust_prev.item_id, 0));
			}
		}

		customer_list_prev.removeAllElements();
	}

	/**
	 * Method getCustomerList - Get customer list
	 *
	 *
	 * @return Vector - Customer List
	 *
	 */
	public Vector getCustomerList() {
		return customer_list;
	}

	/**
	 * Method resetCustomerList - Remove all items in customer list
	 *
	 *
	 */
	public void resetCustomerList() {
		customer_list.removeAllElements();
	}

	/**
	 * Method setOrderNumber - Set order number. 
	 *
	 *
	 * @param order_num  Order number
	 *
	 */
	public void setOrderNumber(String order_num) {
		order_number = order_num;
	}

	/**
	 * Method getOrderNumber - Get order number
	 *
	 *
	 * @return String - Order number
	 *
	 */
	public String getOrderNumber() {
		return order_number;
	}

	/**
	 * Method setCustomerID - Set customer id
	 *
	 *
	 * @param cust_id Customer id
	 *
	 */
	public void setCustomerID(String cust_id) {
		customer_id = cust_id;
	}

	/**
	 * Method getCustomerID - Get customer id
	 *
	 *
	 * @return String - Customer id
	 *
	 */
	public String getCustomerID() {
		return customer_id;
	}
}
