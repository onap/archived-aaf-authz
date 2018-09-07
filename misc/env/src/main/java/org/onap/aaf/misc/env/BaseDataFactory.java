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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.onap.aaf.misc.env.impl.EnvFactory;
import org.xml.sax.SAXException;



/**
 * DataFactory Constructor will create the Stringifiers and Objectifiers necessary 
 * by Type and store the Class of the Type for quick creation of Data Objects
 * with reused (and thread safe) components
 * s
 * Native Types are included.
 * Those types covered by Env Implementation are covered dynamically.
 * Types outside of Env mechanism can be added with "add" function
 * 
 * @author Jonathan
 *
 * @param <T>
 */
public class BaseDataFactory {
    private static final Object LOCK = new Object();
    /**
     * Generate a Schema Object for use in validation based on FileNames.
     * 
     * WARNING: The java.xml.binding code requires YOU to figure out what order the
     * files go in.  If there is an import from A in B, then you must list A first.
     * 
     * @param err
     * @param filenames
     * @return
     * @throws APIException
     */
    public static Schema genSchema(Store env, String ... filenames) throws APIException {
        String schemaDir = env.get(
                env.staticSlot(EnvFactory.SCHEMA_DIR),
                EnvFactory.DEFAULT_SCHEMA_DIR);
        File dir = new File(schemaDir);
        if (!dir.exists())throw new APIException("Schema Directory " + schemaDir + " does not exist.  You can set this with " + EnvFactory.SCHEMA_DIR + " property");
        FileInputStream[] fis = new FileInputStream[filenames.length];
        Source[] sources = new Source[filenames.length];
        File f; 
        for (int i=0; i<filenames.length; ++i) {
            if (!(f=new File(schemaDir + File.separatorChar + filenames[i])).exists()) {
                if (!f.exists()) throw new APIException("Cannot find " + f.getName() + " for schema validation");
            }
            try {
                fis[i]=new FileInputStream(f);
            } catch (FileNotFoundException e) {
                throw new APIException(e);
            }
            sources[i]= new StreamSource(fis[i]);
        }
        try {
            //Note: SchemaFactory is not reentrant or very thread safe either... see docs
            synchronized(LOCK) { // SchemaFactory is not reentrant
                return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                     .newSchema(sources);
            }
        } catch (SAXException e) {
            throw new APIException(e);
        } finally {
            for (FileInputStream d : fis) {
                try {
                    d.close();
                } catch (IOException e) {
                 // Never mind... we did our best
                }
            }
        }

    }

    public static QName getQName(Class<?> clss) throws APIException {
        // Obtain the Necessary info for QName from Requirement
        XmlRootElement xre = clss.getAnnotation(XmlRootElement.class);
        if (xre==null)throw new APIException(clss.getName() + " does not have an XmlRootElement annotation");
        Package pkg = clss.getPackage();
        XmlSchema xs = pkg.getAnnotation(XmlSchema.class);
        if (xs==null) throw new APIException(clss.getName() + " package-info does not have an XmlSchema annotation");
        return new QName(xs.namespace(),xre.name());
    }

    /////////////////////////////////////////////
    // Native Type Converters
    /////////////////////////////////////////////
//    /**
//     * StringStringifier
//     * 
//     * Support the Native Type String.. just return it back
//     * 
//     * @author Jonathan
//     *
//     */
//    public static class StringStringifier extends NullLifeCycle implements Stringifier<String> {
//        /* (non-Javadoc)
//         * @see com.att.env.Stringifier#stringify(com.att.env.Env, java.lang.Object)
//         */
//        public String stringify(Env env, String input) throws APIException {
//            return input;
//        }
//    };        
//
//    /**
//     * StringObjectifier
//     * 
//     * Support the Native Type String.. just return it back
//     * 
//     * @author Jonathan
//     *
//     */
//    public static class StringObjectifier extends NullLifeCycle implements Objectifier<String> {
//        /* (non-Javadoc)
//         * @see com.att.env.Objectifier#objectify(com.att.env.Env, java.lang.String)
//         */
//        public String objectify(Env env, String input) throws APIException {
//            return input;
//        }
//
//        /* (non-Javadoc)
//         * @see com.att.env.Objectifier#newObject()
//         */
//        public String newInstance() throws APIException {
//            return "";
//        }
//    };
//    
//    /**
//     * LongStringifier
//     * 
//     * Support the Native Type Long.. use Long parse functions
//     * 
//     * @author Jonathan
//     *
//     */
//    public static class LongStringifier extends NullLifeCycle implements Stringifier<Long> {
//        public String stringify(Env env, Long input) throws APIException {
//            return input.toString();
//        }
//    }
//    
//    /**
//     * LongObjectifier
//     * 
//     * Support the Native Type Long.. use Long parse functions
//     * 
//     * @author Jonathan
//     *
//     */
//    public static class LongObjectifier extends NullLifeCycle implements Objectifier<Long> {
//        /* (non-Javadoc)
//         * @see com.att.env.Objectifier#objectify(com.att.env.Env, java.lang.String)
//         */
//        public Long objectify(Env env, String input) throws APIException {
//            try {
//                return new Long(input);
//            } catch (Exception e) {
//                APIException ae = new APIException("Cannot create a \"Long\" from [" + input + ']');
//                ae.initCause(e);
//                throw ae;
//            }
//        }
//
//        /* (non-Javadoc)
//         * @see com.att.env.Objectifier#newObject()
//         */
//        public Long newInstance() throws APIException {
//            return 0L;
//        }
//    }
//
//    /**
//     * IntegerStringifier
//     * 
//     * Support the Native Integer.. use Integer parse functions
//     * 
//     * @author Jonathan
//     *
//     */
//    public static class IntegerStringifier extends NullLifeCycle implements Stringifier<Integer> {
//        /* (non-Javadoc)
//         * @see com.att.env.Stringifier#stringify(com.att.env.Env, java.lang.Object)
//         */
//        public String stringify(Env env, Integer input) throws APIException {
//            return input.toString();
//        }
//    }
//    
//    /**
//     * IntegerObjectifier
//     * 
//     * Support the Native Integer.. use Integer parse functions
//     * 
//     * @author Jonathan
//     *
//     */
//    public static class IntegerObjectifier extends NullLifeCycle implements Objectifier<Integer> {
//        /* (non-Javadoc)
//         * @see com.att.env.Objectifier#objectify(com.att.env.Env, java.lang.String)
//         */
//        public Integer objectify(Env env, String input) throws APIException {
//            try {
//                return new Integer(input);
//            } catch (Exception e) {
//                APIException ae = new APIException("Cannot create a \"Integer\" from [" + input + ']');
//                ae.initCause(e);
//                throw ae;
//            }
//        }
//
//        /* (non-Javadoc)
//         * @see com.att.env.Objectifier#newObject()
//         */
//        public Integer newInstance() throws APIException {
//            return 0;
//        }
//    }
//
//    /**
//     * ShortStringifier
//     * 
//     * Support the Native Short.. use Short parse functions
//     * 
//     * @author Jonathan
//     *
//     */
//    public static class ShortStringifier extends NullLifeCycle implements Stringifier<Short> {
//        public String stringify(Env env, Short input) throws APIException {
//            return input.toString();
//        }
//    }
//    
//    /**
//     * ShortObjectifier
//     * 
//     * Support the Native Short.. use Short parse functions
//     * 
//     * @author Jonathan
//     *
//     */
//    public static class ShortObjectifier extends NullLifeCycle implements Objectifier<Short> {
//        public Short objectify(Env env, String input) throws APIException {
//            try {
//                return new Short(input);
//            } catch (Exception e) {
//                APIException ae = new APIException("Cannot create a \"Short\" from [" + input + ']');
//                ae.initCause(e);
//                throw ae;
//            }
//        }
//
//        public Short newInstance() throws APIException {
//            return 0;
//        }
//    }
//    
//    /**
//     * ByteStringifier
//     * 
//     * Support the Native Byte.. use Byte parse functions
//     * 
//     * @author Jonathan
//     *
//     */
//    public static class ByteStringifier extends NullLifeCycle implements Stringifier<Byte> {
//        /* (non-Javadoc)
//         * @see com.att.env.Stringifier#stringify(com.att.env.Env, java.lang.Object)
//         */
//        public String stringify(Env env, Byte input) throws APIException {
//            return input.toString();
//        }
//    }
//    
//    /**
//     * ByteObjectifier
//     * 
//     * Support the Native Byte.. use Byte parse functions
//     * 
//     * @author Jonathan
//     *
//     */
//    public static class ByteObjectifier extends NullLifeCycle implements Objectifier<Byte> {
//        /* (non-Javadoc)
//         * @see com.att.env.Objectifier#objectify(com.att.env.Env, java.lang.String)
//         */
//        public Byte objectify(Env env, String input) throws APIException {
//            try {
//                return new Byte(input);
//            } catch (Exception e) {
//                APIException ae = new APIException("Cannot create a \"Byte\" from [" + input + ']');
//                ae.initCause(e);
//                throw ae;
//            }
//        }
//
//        /* (non-Javadoc)
//         * @see com.att.env.Objectifier#newObject()
//         */
//        public Byte newInstance() throws APIException {
//            return 0;
//        }
//    }
//
//    /**
//     * CharacterStringifier
//     * 
//     * Support the Native Character.. use Character parse functions
//     * 
//     * @author Jonathan
//     *
//     */
//    public static class CharacterStringifier extends NullLifeCycle implements Stringifier<Character> {
//        /* (non-Javadoc)
//         * @see com.att.env.Stringifier#stringify(com.att.env.Env, java.lang.Object)
//         */
//        public String stringify(Env env, Character input) throws APIException {
//            return input.toString();
//        }
//    }
//    
//    /**
//     * CharacterObjectifier
//     * 
//     * Support the Native Character.. use Character parse functions
//     * 
//     * @author Jonathan
//     *
//     */
//    public static class CharacterObjectifier extends NullLifeCycle implements Objectifier<Character> {
//        /* (non-Javadoc)
//         * @see com.att.env.Objectifier#objectify(com.att.env.Env, java.lang.String)
//         */
//        public Character objectify(Env env, String input) throws APIException {
//            int length = input.length();
//            if (length<1 || length>1) {
//                throw new APIException("String [" + input + "] does not represent a single Character");
//            }
//            return input.charAt(0);
//        }
//
//        /* (non-Javadoc)
//         * @see com.att.env.Objectifier#newObject()
//         */
//        public Character newInstance() throws APIException {
//            return 0;
//        }
//    }
//
//    /**
//     * FloatStringifier
//     * 
//     * Support the Native Float.. use Float parse functions
//     * 
//     * @author Jonathan
//     *
//     */
//    public static class FloatStringifier extends NullLifeCycle implements Stringifier<Float> {
//        /* (non-Javadoc)
//         * @see com.att.env.Stringifier#stringify(com.att.env.Env, java.lang.Object)
//         */
//        public String stringify(Env env, Float input) throws APIException {
//            return input.toString();
//        }
//    }
//    
//    /**
//     * FloatObjectifier
//     * 
//     * Support the Native Float.. use Float parse functions
//     * 
//     * @author Jonathan
//     *
//     */
//    public static class FloatObjectifier extends NullLifeCycle implements Objectifier<Float> {
//        /* (non-Javadoc)
//         * @see com.att.env.Objectifier#objectify(com.att.env.Env, java.lang.String)
//         */
//        public Float objectify(Env env, String input) throws APIException {
//            try {
//                return new Float(input);
//            } catch (Exception e) {
//                APIException ae = new APIException("Cannot create a \"Float\" from [" + input + ']');
//                ae.initCause(e);
//                throw ae;
//            }
//        }
//
//        /* (non-Javadoc)
//         * @see com.att.env.Objectifier#newObject()
//         */
//        public Float newInstance() throws APIException {
//            return 0.0f;
//        }
//    }
//
//    /**
//     * DoubleStringifier
//     * 
//     * Support the Native Double.. use Double parse functions
//     * 
//     * @author Jonathan
//     *
//     */
//    public static class DoubleStringifier extends NullLifeCycle implements Stringifier<Double> {
//        /* (non-Javadoc)
//         * @see com.att.env.Stringifier#stringify(com.att.env.Env, java.lang.Object)
//         */
//        public String stringify(Env env, Double input) throws APIException {
//            return input.toString();
//        }
//    }
//    
//    /**
//     * DoubleObjectifier
//     * 
//     * Support the Native Double.. use Double parse functions
//     * 
//     * @author Jonathan
//     *
//     */
//    public static class DoubleObjectifier extends NullLifeCycle implements Objectifier<Double> {
//        /* (non-Javadoc)
//         * @see com.att.env.Objectifier#objectify(com.att.env.Env, java.lang.String)
//         */
//        public Double objectify(Env env, String input) throws APIException {
//            try {
//                return new Double(input);
//            } catch (Exception e) {
//                APIException ae = new APIException("Cannot create a \"Double\" from [" + input + ']');
//                ae.initCause(e);
//                throw ae;
//            }
//        }
//
//        /* (non-Javadoc)
//         * @see com.att.env.Objectifier#newObject()
//         */
//        public Double newInstance() throws APIException {
//            return 0.0;
//        }
//    }

}
