
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: CartBean.java,v 1.1 2004/02/19 14:45:05 emrek Exp $
 *
 */
package com.sun.ecperf.webbeans;


import javax.ejb.*;

import javax.naming.*;

import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject;

import com.sun.ecperf.orders.itement.ejb.*;
import com.sun.ecperf.orders.helper.*;
import com.sun.ecperf.orders.cartses.ejb.*;
import com.sun.ecperf.common.*;

import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;


/**
 * This is a helper bean used by jsp files to  create a new cart,
 * add and remove items to the cart, and order it. It uses stateful
 * session bean "CartSes" for doing this.
 *
 * @author Venkata Yella - yella@eng.sun.com
 *
 */
public class CartBean implements java.io.Serializable {

	Vector                      items_list;
	Vector                      item_name_id_list;
	ItemEntHome                 itement_home;
	CartSesHome                 cartses_home;
	CartSes                     cartses;

	protected Debug             debug;
	protected boolean           debugging;

	private static final String jndiname_itement =
		"java:comp/env/ejb/ItemEnt";
	private static final String jndiname_cartses =
		"java:comp/env/ejb/CartSes";

	/**
	 * Constructor CartBean
	 *
	 *
	 * @throws OtherException
	 *
	 */
	public CartBean() throws OtherException {

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
*/
			if (debugLevel > 0) {
				debug = new DebugPrint(debugLevel, this);
				debugging = true;
			} else {
				debug = new Debug();
				debugging = false;
			}
			if(debugging)
				debug.println(3, "In constructor of CartBean");

			Object obj_itement = context.lookup(jndiname_itement);

			if(debugging)
				debug.println(3, "Looked up " + jndiname_itement);

			itement_home =
				(ItemEntHome) PortableRemoteObject.narrow(obj_itement,
					ItemEntHome.class);

			Object obj_cartses = context.lookup(jndiname_cartses);

			if(debugging)
				debug.println(3, "Looked up " + jndiname_cartses);

			cartses_home =
				(CartSesHome) PortableRemoteObject.narrow(obj_cartses,
					CartSesHome.class);
			cartses      = cartses_home.create();
		} catch (NamingException e) {
			throw new OtherException("Naming Exception in CartBean", e);
		} catch (ClassCastException e) {
			throw new OtherException("Class cast Exception in CartBean", e);
		} catch (RemoteException e) {
			throw new OtherException("Remote Exception in CartBean", e);
		} catch (CreateException e) {
			throw new OtherException("Create Exception in CartBean", e);
		} catch (Exception e) {
			throw new OtherException("Some Other  Exception in CartBean", e);
		}
	}

	/*
	 * This method is called the very first time. After that the getList method
	 * is called.
	 */

	/**
	 * Method getItemsList - This method is called the very first time. 
	 *
	 *
	 * @return Vector - List of all items
	 *
	 * @throws OtherException 
	 *
	 */
	public Vector getItemsList() throws OtherException {

		if (items_list == null) {
			try {
				ItemEnt     itement;
				Enumeration items;

				if(debugging)
					debug.println(3, "Find all ItemEnt beans");

				items             = itement_home.findAll();
				items_list        = new Vector();
				item_name_id_list = new Vector();

				// Removed vendor specific entries and used 
                                // ItemID as name instead of random generated name.
				while (items.hasMoreElements()) {
					itement =
						(ItemEnt) javax.rmi.PortableRemoteObject
							.narrow(items.nextElement(), ItemEnt.class);

					if(debugging)
						debug.println(3, "Item Id and price are "
								  + itement.getId() + " "
								  + itement.getPrice());

					      items_list.add(itement.getId() + "(Price - " + itement.getPrice()+")");
					      item_name_id_list.add(new ItemNameId(itement.getId(), itement.getId()));
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
	 * Method addItem - Add an item to the list of current items
	 *
	 *
	 * @param item_str Item string from the list
	 * @param quantity Quantity of the item
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
			ItemQuantity item_qty = new ItemQuantity(item_id, qty);

			try {
				cartses.add(item_qty);
			} catch (RemoteException e) {
				throw new OtherException(
					" Remote Exception in adding to CartSes Bean", e);
			}
		}
	}

	/**
	 * Method removeItem - Remove an item from the list of current items.
	 *
	 *
	 * @param item_str Item string from the list
	 * @param quantity Quantity of the item
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
			ItemQuantity item_qty = new ItemQuantity(item_id, qty);

			try {
				cartses.removeItem(item_qty);
			} catch (RemoteException e) {
				throw new OtherException(
					" Remote Exception in removing from CartSes Bean", e);
			}
		}
	}

	/**
	 * Method newOrder - Create a new order for the current list of items.
	 *
	 *
	 * @return int - Order number
	 *
	 * @throws OtherException
	 *
	 */
	public int newOrder() throws OtherException {

		try {
			return cartses.buy();
		} catch (RemoteException e) {
			throw new OtherException(
				" Remote Exception in creating a new order in CartSes Bean",
				e);
		} catch (CreateException e) {
			throw new OtherException(
				" Create Exception in creating a new order in CartSes Bean",
				e);
		} catch (InsufficientCreditException e) {
			throw new OtherException(
				" Remote Exception in creating a new order in CartSes Bean",
				e);
		}
	}

	/**
	 * Method getCustomerList - Get a list of all items for the customer
	 *
	 *
	 * @return Vector - A list of all items
	 *
	 * @throws OtherException
	 *
	 */
	public Vector getCustomerList() throws OtherException {

		Vector       customer_list;
		Vector       cur_items_list;
		ItemQuantity cur_i_qty;
		ItemNameId   i_name_id;

		customer_list = new Vector();

		try {
			cur_items_list = cartses.getItemsList();
		} catch (RemoteException e) {
			throw new OtherException(
				" Remote Exception in getting items list from CartSes Bean",
				e);
		}

		for (int i = 0; i < cur_items_list.size(); i++) {
			cur_i_qty = (ItemQuantity) cur_items_list.elementAt(i);

			for (int j = 0; j < item_name_id_list.size(); j++) {
				i_name_id = (ItemNameId) item_name_id_list.elementAt(j);

				if (cur_i_qty.itemId.equals(i_name_id.item_id)) {
					customer_list
						.add(new CustomerItem(i_name_id.item_name,
											  cur_i_qty.itemId,
											  cur_i_qty.itemQuantity));

					break;
				}
			}
		}

		return customer_list;
	}

	/**
	 * Method resetCustomerList - Remove all items in the customer list
	 *
	 *
	 * @throws OtherException
	 *
	 */
	public void resetCustomerList() throws OtherException {

		try {
			cartses.deleteAll();
		} catch (RemoteException e) {
			throw new OtherException(
				" Remote Exception in deleting items from CartSes Bean", e);
		}
	}

	/**
	 * Method setCustomerID - Set the Id of the customer
	 *
	 *
	 * @param customer_id String Id of the customer
	 *
	 * @throws InvalidEntryException If the id is not a valid entry
	 * @throws OtherException
	 *
	 */
	public void setCustomerID(String customer_id)
			throws OtherException, InvalidEntryException {

		int cust_id;

		try {
			cust_id = Integer.parseInt(customer_id);
		} catch (NumberFormatException e) {
			throw new InvalidEntryException(
				"Customer Entry " + customer_id
				+ " is invalid. Please try again");
		}

		try {
			cartses.setCustId(cust_id);
		} catch (RemoteException e) {
			throw new OtherException(
				" Remote Exception in setting customer id in CartSes Bean",
				e);
		}
	}
}
