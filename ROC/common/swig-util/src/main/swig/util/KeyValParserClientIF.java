/* Copyright  (c) 2002 The Board of Trustees of The Leland Stanford Junior
 * University. All Rights Reserved.
 *
 * See the file LICENSE for information on redistributing this software.
 */

package swig.util;

public interface KeyValParserClientIF {
    void hashHandler(
        String first,
        String second,
        KeyValParser kvParser,
        String flag);
}