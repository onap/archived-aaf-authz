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

package org.onap.aaf.misc.rosetta;

import java.io.IOException;
import java.io.Reader;
import java.util.Map.Entry;
import java.util.TreeMap;

public class XmlEscape {
    private XmlEscape() {}

    private static final TreeMap<String,Integer> charMap; // see initialization at end
    private static final TreeMap<Integer,String> intMap; // see initialization at end

    public static void xmlEscape(StringBuilder sb, Reader r) throws ParseException {
        try {
            int c;
            StringBuilder esc = new StringBuilder();
            for (int cnt = 0;cnt<9 /*max*/; ++cnt) {
                if ((c=r.read())<0)throw new ParseException("Invalid Data: Unfinished Escape Sequence");
                if (c!=';') {
                    esc.append((char)c);
                } else { // evaluate
                    Integer i = charMap.get(esc.toString());
                    if (i==null) {
                        // leave in nasty XML format for now.
                        sb.append('&');
                        sb.append(esc);
                        sb.append(';');
                    } else {
                        sb.append((char)i.intValue());
                    }
                    break;
                }
            }


        } catch (IOException e) {
            throw new ParseException(e);
        }
    }

    public static void xmlEscape(StringBuilder sb, int chr) {
        sb.append('&');
        sb.append(intMap.get(chr));
        sb.append(';');
    }

    public static String convert(StringBuilder insb) {
        int idx, ch;
        StringBuilder sb=null;
        for (idx=0;idx<insb.length();++idx) {
            ch = insb.charAt(idx);
            if (ch>=160 || ch==34 || ch==38 || ch==39 || ch==60 || ch==62) {
                sb = new StringBuilder();
                sb.append(insb,0,idx);
                break;
            }
        }

        if (sb==null)return insb.toString();

        for (int i=idx;i<insb.length();++i) {
            ch = insb.charAt(i);
            if (ch<160) {
                switch(ch) {
                    case 34: sb.append("&quot;"); break;
                    case 38: sb.append("&amp;"); break;
                    case 39: sb.append("&apos;"); break;
                    case 60: sb.append("&lt;"); break;
                    case 62: sb.append("&gt;"); break;
                    default:
                        sb.append((char)ch);
                }
            } else { // use map
                String s = intMap.get(ch);
                if (s==null)sb.append((char)ch);
                else {
                    sb.append('&');
                    sb.append(s);
                    sb.append(';');
                }
            }
        }
        return sb.toString();
    }

    static {
        charMap = new TreeMap<>();
        intMap = new TreeMap<>();
        charMap.put("quot", 34);
        charMap.put("amp",38);
        charMap.put("apos",39);
        charMap.put("lt",60);
        charMap.put("gt",62);
        charMap.put("nbsp",160);
        charMap.put("iexcl",161);
        charMap.put("cent",162);
        charMap.put("pound",163);
        charMap.put("curren",164);
        charMap.put("yen",165);
        charMap.put("brvbar",166);
        charMap.put("sect",167);
        charMap.put("uml",168);
        charMap.put("copy",169);
        charMap.put("ordf",170);
        charMap.put("laquo",171);
        charMap.put("not",172);
        charMap.put("shy",173);
        charMap.put("reg",174);
        charMap.put("macr",175);
        charMap.put("deg",176);
        charMap.put("plusmn",177);
        charMap.put("sup2",178);
        charMap.put("sup3",179);
        charMap.put("acute",180);
        charMap.put("micro",181);
        charMap.put("para",182);
        charMap.put("middot",183);
        charMap.put("cedil",184);
        charMap.put("sup1",185);
        charMap.put("ordm",186);
        charMap.put("raquo",187);
        charMap.put("frac14",188);
        charMap.put("frac12",189);
        charMap.put("frac34",190);
        charMap.put("iquest",191);
        charMap.put("Agrave",192);
        charMap.put("Aacute",193);
        charMap.put("Acirc",194);
        charMap.put("Atilde",195);
        charMap.put("Auml",196);
        charMap.put("Aring",197);
        charMap.put("AElig",198);
        charMap.put("Ccedil",199);
        charMap.put("Egrave",200);
        charMap.put("Eacute",201);
        charMap.put("Ecirc",202);
        charMap.put("Euml",203);
        charMap.put("Igrave",204);
        charMap.put("Iacute",205);
        charMap.put("Icirc",206);
        charMap.put("Iuml",207);
        charMap.put("ETH",208);
        charMap.put("Ntilde",209);
        charMap.put("Ograve",210);
        charMap.put("Oacute",211);
        charMap.put("Ocirc",212);
        charMap.put("Otilde",213);
        charMap.put("Ouml",214);
        charMap.put("times",215);
        charMap.put("Oslash",216);
        charMap.put("Ugrave",217);
        charMap.put("Uacute",218);
        charMap.put("Ucirc",219);
        charMap.put("Uuml",220);
        charMap.put("Yacute",221);
        charMap.put("THORN",222);
        charMap.put("szlig",223);
        charMap.put("agrave",224);
        charMap.put("aacute",225);
        charMap.put("acirc",226);
        charMap.put("atilde",227);
        charMap.put("auml",228);
        charMap.put("aring",229);
        charMap.put("aelig",230);
        charMap.put("ccedil",231);
        charMap.put("egrave",232);
        charMap.put("eacute",233);
        charMap.put("ecirc",234);
        charMap.put("euml",235);
        charMap.put("igrave",236);
        charMap.put("iacute",237);
        charMap.put("icirc",238);
        charMap.put("iuml",239);
        charMap.put("eth",240);
        charMap.put("ntilde",241);
        charMap.put("ograve",242);
        charMap.put("oacute",243);
        charMap.put("ocirc",244);
        charMap.put("otilde",245);
        charMap.put("ouml",246);
        charMap.put("divide",247);
        charMap.put("oslash",248);
        charMap.put("ugrave",249);
        charMap.put("uacute",250);
        charMap.put("ucirc",251);
        charMap.put("uuml",252);
        charMap.put("yacute",253);
        charMap.put("thorn",254);
        charMap.put("yuml",255);
        charMap.put("OElig",338);
        charMap.put("oelig",339);
        charMap.put("Scaron",352);
        charMap.put("scaron",353);
        charMap.put("Yuml",376);
        charMap.put("fnof",402);
        charMap.put("circ",710);
        charMap.put("tilde",732);
        charMap.put("Alpha",913);
        charMap.put("Beta",914);
        charMap.put("Gamma",915);
        charMap.put("Delta",916);
        charMap.put("Epsilon",917);
        charMap.put("Zeta",918);
        charMap.put("Eta",919);
        charMap.put("Theta",920);
        charMap.put("Iota",921);
        charMap.put("Kappa",922);
        charMap.put("Lambda",923);
        charMap.put("Mu",924);
        charMap.put("Nu",925);
        charMap.put("Xi",926);
        charMap.put("Omicron",927);
        charMap.put("Pi",928);
        charMap.put("Rho",929);
        charMap.put("Sigma",931);
        charMap.put("Tau",932);
        charMap.put("Upsilon",933);
        charMap.put("Phi",934);
        charMap.put("Chi",935);
        charMap.put("Psi",936);
        charMap.put("Omega",937);
        charMap.put("alpha",945);
        charMap.put("beta",946);
        charMap.put("gamma",947);
        charMap.put("delta",948);
        charMap.put("epsilon",949);
        charMap.put("zeta",950);
        charMap.put("eta",951);
        charMap.put("theta",952);
        charMap.put("iota",953);
        charMap.put("kappa",954);
        charMap.put("lambda",955);
        charMap.put("mu",956);
        charMap.put("nu",957);
        charMap.put("xi",958);
        charMap.put("omicron",959);
        charMap.put("pi",960);
        charMap.put("rho",961);
        charMap.put("sigmaf",962);
        charMap.put("sigma",963);
        charMap.put("tau",964);
        charMap.put("upsilon",965);
        charMap.put("phi",966);
        charMap.put("chi",967);
        charMap.put("psi",968);
        charMap.put("omega",969);
        charMap.put("thetasym",977);
        charMap.put("upsih",978);
        charMap.put("piv",982);
        charMap.put("ensp",8194);
        charMap.put("emsp",8195);
        charMap.put("thinsp",8201);
        charMap.put("zwnj",8204);
        charMap.put("zwj",8205);
        charMap.put("lrm",8206);
        charMap.put("rlm",8207);
        charMap.put("ndash",8211);
        charMap.put("mdash",8212);
        charMap.put("lsquo",8216);
        charMap.put("rsquo",8217);
        charMap.put("sbquo",8218);
        charMap.put("ldquo",8220);
        charMap.put("rdquo",8221);
        charMap.put("bdquo",8222);
        charMap.put("dagger",8224);
        charMap.put("Dagger",8225);
        charMap.put("bull",8226);
        charMap.put("hellip",8230);
        charMap.put("permil",8240);
        charMap.put("prime",8242);
        charMap.put("Prime",8243);
        charMap.put("lsaquo",8249);
        charMap.put("rsaquo",8250);
        charMap.put("oline",8254);
        charMap.put("frasl",8260);
        charMap.put("euro",8364);
        charMap.put("image",8465);
        charMap.put("weierp",8472);
        charMap.put("real",8476);
        charMap.put("trade",8482);
        charMap.put("alefsym",8501);
        charMap.put("larr",8592);
        charMap.put("uarr",8593);
        charMap.put("rarr",8594);
        charMap.put("darr",8595);
        charMap.put("harr",8596);
        charMap.put("crarr",8629);
        charMap.put("lArr",8656);
        charMap.put("uArr",8657);
        charMap.put("rArr",8658);
        charMap.put("dArr",8659);
        charMap.put("hArr",8660);
        charMap.put("forall",8704);
        charMap.put("part",8706);
        charMap.put("exist",8707);
        charMap.put("empty",8709);
        charMap.put("nabla",8711);
        charMap.put("isin",8712);
        charMap.put("notin",8713);
        charMap.put("ni",8715);
        charMap.put("prod",8719);
        charMap.put("sum",8721);
        charMap.put("minus",8722);
        charMap.put("lowast",8727);
        charMap.put("radic",8730);
        charMap.put("prop",8733);
        charMap.put("infin",8734);
        charMap.put("ang",8736);
        charMap.put("and",8743);
        charMap.put("or",8744);
        charMap.put("cap",8745);
        charMap.put("cup",8746);
        charMap.put("int",8747);
        charMap.put("there4",8756);
        charMap.put("sim",8764);
        charMap.put("cong",8773);
        charMap.put("asymp",8776);
        charMap.put("ne",8800);
        charMap.put("equiv",8801);
        charMap.put("le",8804);
        charMap.put("ge",8805);
        charMap.put("sub",8834);
        charMap.put("sup",8835);
        charMap.put("nsub",8836);
        charMap.put("sube",8838);
        charMap.put("supe",8839);
        charMap.put("oplus",8853);
        charMap.put("otimes",8855);
        charMap.put("perp",8869);
        charMap.put("sdot",8901);
        charMap.put("lceil",8968);
        charMap.put("rceil",8969);
        charMap.put("lfloor",8970);
        charMap.put("rfloor",8971);
        charMap.put("lang",9001);
        charMap.put("rang",9002);
        charMap.put("loz",9674);
        charMap.put("spades",9824);
        charMap.put("clubs",9827);
        charMap.put("hearts",9829);
        charMap.put("diams",9830);

        for ( Entry<String, Integer> es: charMap.entrySet()) {
            if (es.getValue()>=160); // save small space... note that no longer has amp, etc.
            intMap.put(es.getValue(), es.getKey());
        }
    }

}
