/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.misc.env;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
/**
 * <H1>Data</H1>
 * <i>Data</i> facilitates lazy marshaling of data with a pre-determined
 * marshaling mechanism.<p>
 * 
 * It stores either Object (defined by Generic {@literal <T>}) or String.<p>  
 * 
 * On asking for Object of type {@literal <T>}, it will respond with the object
 * if it exists, or unmarshal the string and pass the result back.<p>
 * 
 * On asking for String, it will respond with the String
 * if it exists, or marshal the String and pass the result back.<p>
 *
 * the "options" available on several functions control the output of this particular call.  When 
 * blank, they will default to the DataFactory defaults.  When present, they override this
 * particular call.
 *     The available options are "pretty" (for XML and JSON) and "fragment" (XML only concept), which drops
 * the "<?xml ...?>" header so you can create larger XML documents from the output. 
 * 
 * @author Jonathan
 *
 * @param <T>
 */
public interface Data<T> {
     enum TYPE {XML,JSON,JAXB,RAW,DEFAULT};
    // can & with 0xFFFF;
//    public static final int XML = 0x1;
//    public static final int JSON = 0x2;
//    public static final int JAXB = 0x4;
//    public static final int RAW = 0x1000;
    
    // can & with 0xF00000;
    public static final int PRETTY = 0x100000;
    public static final int FRAGMENT = 0x200000;

    /**
     * Respond with the String if it exists, or marshal the String and pass the result back.
     * 
     * However, use the Env the Data Object was created with.
     * 
     * @return String
     * @throws APIException
     */
    public String asString() throws APIException;

    /**
     * Respond with the Object of type {@literal <T>} if it exists, or unmarshal from String 
     * and pass the result back.<p>
     *
     * However, use the Env the Data Object was created with.
     * 
     * @return T
     * @throws APIException
     */
    public T asObject() throws APIException;

    /**
     * Set a particular option on an existing Out 
     * 
     * if int is negative, it should remove the option
     * @param option
     */
    public Data<T> option(int option);

    public Data<T> to(OutputStream os) throws APIException, IOException;
    public Data<T> to(Writer writer) throws APIException, IOException;
    
    public Data<T> load(T t) throws APIException;
    public Data<T> load(String str) throws APIException;
    public Data<T> load(InputStream is) throws APIException;
    public Data<T> load(Reader rdr) throws APIException;
    
    public Data<T> in(TYPE type);
    public Data<T> out(TYPE type);
    /**
     * Return the Class Type supported by this DataObject
     * 
     * @return {@literal Class<T>}
     */
    public Class<T> getTypeClass();

    public void direct(InputStream input, OutputStream output) throws APIException, IOException;


}