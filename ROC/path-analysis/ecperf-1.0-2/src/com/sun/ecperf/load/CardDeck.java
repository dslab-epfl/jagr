/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: CardDeck.java,v 1.1.1.1 2002/11/16 05:35:26 emrek Exp $
 *
 */
package com.sun.ecperf.load;
import java.lang.*;
import java.util.*;

/**
 * This file creates and maintains a card deck with specified 
 * number of items. Note that this currently only works for integers
 * 
 * @author Shanti Subramanyam
 */
public class CardDeck {
	private Random r;
	Vector deck, usedDeck;


/*
 * Constructor
 * Initialize card deck
 */
    CardDeck(int start, int end)
    {
	int range;
        r = new Random();
        range = end - start + 1;
        deck = new Vector(range);
        usedDeck = new Vector(range);
	for (int i = start; i <= end; i++)
            deck.addElement(new Integer(i));
    }



    /**
     * Select a random number from our deck
     */
    int nextCard()
    {
        int n; 
        Integer card;
        if (deck.isEmpty()) {
            Vector tmp = deck;
            deck = usedDeck;
            usedDeck = tmp;
        }
        n = r.nextInt(deck.size());
        card = (Integer)deck.elementAt(n);
        deck.removeElementAt(n);
        usedDeck.addElement(card);
        return(card.intValue()); 
    } 

}
