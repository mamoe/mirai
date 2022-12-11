/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

@Suppress("RegExpRedundantEscape")
private val STR_TO_CHAR_PATTERN = """\&(\#?[A-Za-z0-9]+?)\;""".toRegex()

public fun String.decodeHtmlEscape(): String = replace(STR_TO_CHAR_PATTERN) { match ->
    STR_TO_CHAR_MAPPINGS[match.value]?.let { return@replace it }
    val match1 = match.groups[1]!!.value
    if (match1.length > 1 && match1[0] == '#') {
        if (match1.length > 2) {
            if (match1[1] == 'x') { // hex
                match1.substring(2).toIntOrNull(16)?.let {
                    return@replace it.toChar().toString()
                }
            }
        }
        match1.substring(1).toIntOrNull()?.let {
            return@replace it.toChar().toString()
        }
    }

    match.value
}

public fun String.encodeHtmlEscape(): String = buildString(length) {
    this@encodeHtmlEscape.forEach { c ->
        if (needDoHtmlEscape(c)) {
            append("&#").append(c.code).append(';')
        } else {
            append(c)
        }
    }
}

private fun needDoHtmlEscape(c: Char): Boolean {
    if (c.code < 32) return true // Ascii control codes

    if (c in "#@!~$%^&*()<>/\\\"'") return true
    return false
}


private val STR_TO_CHAR_MAPPINGS: Map<String, String> by lazy {
//<editor-fold defaultstate="collapsed" desc="Generated Code">
    val result = HashMap<String, String>(223)
    result["&amp;"] = "\u0026"
    result["&lt;"] = "\u003c"
    result["&gt;"] = "\u003e"
    result["&nbsp;"] = "\u00a0"
    result["&iexcl;"] = "\u00a1"
    result["&cent;"] = "\u00a2"
    result["&pound;"] = "\u00a3"
    result["&curren;"] = "\u00a4"
    result["&yen;"] = "\u00a5"
    result["&brvbar;"] = "\u00a6"
    result["&sect;"] = "\u00a7"
    result["&uml;"] = "\u00a8"
    result["&copy;"] = "\u00a9"
    result["&ordf;"] = "\u00aa"
    result["&laquo;"] = "\u00ab"
    result["&not;"] = "\u00ac"
    result["&shy;"] = "\u00ad"
    result["&reg;"] = "\u00ae"
    result["&macr;"] = "\u00af"
    result["&deg;"] = "\u00b0"
    result["&plusmn;"] = "\u00b1"
    result["&sup2;"] = "\u00b2"
    result["&sup3;"] = "\u00b3"
    result["&acute;"] = "\u00b4"
    result["&micro;"] = "\u00b5"
    result["&para;"] = "\u00b6"
    result["&middot;"] = "\u00b7"
    result["&cedil;"] = "\u00b8"
    result["&sup1;"] = "\u00b9"
    result["&ordm;"] = "\u00ba"
    result["&raquo;"] = "\u00bb"
    result["&frac14;"] = "\u00bc"
    result["&frac12;"] = "\u00bd"
    result["&frac34;"] = "\u00be"
    result["&iquest;"] = "\u00bf"
    result["&Agrave;"] = "\u00c0"
    result["&Aacute;"] = "\u00c1"
    result["&Acirc;"] = "\u00c2"
    result["&Atilde;"] = "\u00c3"
    result["&Auml;"] = "\u00c4"
    result["&Aring;"] = "\u00c5"
    result["&AElig;"] = "\u00c6"
    result["&Ccedil;"] = "\u00c7"
    result["&Egrave;"] = "\u00c8"
    result["&Eacute;"] = "\u00c9"
    result["&Ecirc;"] = "\u00ca"
    result["&Euml;"] = "\u00cb"
    result["&Igrave;"] = "\u00cc"
    result["&Iacute;"] = "\u00cd"
    result["&Icirc;"] = "\u00ce"
    result["&Iuml;"] = "\u00cf"
    result["&ETH;"] = "\u00d0"
    result["&Ntilde;"] = "\u00d1"
    result["&Ograve;"] = "\u00d2"
    result["&Oacute;"] = "\u00d3"
    result["&Ocirc;"] = "\u00d4"
    result["&Otilde;"] = "\u00d5"
    result["&Ouml;"] = "\u00d6"
    result["&times;"] = "\u00d7"
    result["&Oslash;"] = "\u00d8"
    result["&Ugrave;"] = "\u00d9"
    result["&Uacute;"] = "\u00da"
    result["&Ucirc;"] = "\u00db"
    result["&Uuml;"] = "\u00dc"
    result["&Yacute;"] = "\u00dd"
    result["&THORN;"] = "\u00de"
    result["&szlig;"] = "\u00df"
    result["&agrave;"] = "\u00e0"
    result["&aacute;"] = "\u00e1"
    result["&acirc;"] = "\u00e2"
    result["&atilde;"] = "\u00e3"
    result["&auml;"] = "\u00e4"
    result["&aring;"] = "\u00e5"
    result["&aelig;"] = "\u00e6"
    result["&ccedil;"] = "\u00e7"
    result["&egrave;"] = "\u00e8"
    result["&eacute;"] = "\u00e9"
    result["&ecirc;"] = "\u00ea"
    result["&euml;"] = "\u00eb"
    result["&igrave;"] = "\u00ec"
    result["&iacute;"] = "\u00ed"
    result["&icirc;"] = "\u00ee"
    result["&iuml;"] = "\u00ef"
    result["&eth;"] = "\u00f0"
    result["&ntilde;"] = "\u00f1"
    result["&ograve;"] = "\u00f2"
    result["&oacute;"] = "\u00f3"
    result["&ocirc;"] = "\u00f4"
    result["&otilde;"] = "\u00f5"
    result["&ouml;"] = "\u00f6"
    result["&divide;"] = "\u00f7"
    result["&oslash;"] = "\u00f8"
    result["&ugrave;"] = "\u00f9"
    result["&uacute;"] = "\u00fa"
    result["&ucirc;"] = "\u00fb"
    result["&uuml;"] = "\u00fc"
    result["&yacute;"] = "\u00fd"
    result["&thorn;"] = "\u00fe"
    result["&yuml;"] = "\u00ff"
    result["&fnof;"] = "\u0192"
    result["&Alpha;"] = "\u0391"
    result["&Beta;"] = "\u0392"
    result["&Gamma;"] = "\u0393"
    result["&Delta;"] = "\u0394"
    result["&Epsilon;"] = "\u0395"
    result["&Zeta;"] = "\u0396"
    result["&Eta;"] = "\u0397"
    result["&Theta;"] = "\u0398"
    result["&Iota;"] = "\u0399"
    result["&Kappa;"] = "\u039a"
    result["&Lambda;"] = "\u039b"
    result["&Mu;"] = "\u039c"
    result["&Nu;"] = "\u039d"
    result["&Xi;"] = "\u039e"
    result["&Omicron;"] = "\u039f"
    result["&Pi;"] = "\u03a0"
    result["&Rho;"] = "\u03a1"
    result["&Sigma;"] = "\u03a3"
    result["&Tau;"] = "\u03a4"
    result["&Upsilon;"] = "\u03a5"
    result["&Phi;"] = "\u03a6"
    result["&Chi;"] = "\u03a7"
    result["&Psi;"] = "\u03a8"
    result["&Omega;"] = "\u03a9"
    result["&alpha;"] = "\u03b1"
    result["&beta;"] = "\u03b2"
    result["&gamma;"] = "\u03b3"
    result["&delta;"] = "\u03b4"
    result["&epsilon;"] = "\u03b5"
    result["&zeta;"] = "\u03b6"
    result["&eta;"] = "\u03b7"
    result["&theta;"] = "\u03b8"
    result["&iota;"] = "\u03b9"
    result["&kappa;"] = "\u03ba"
    result["&lambda;"] = "\u03bb"
    result["&mu;"] = "\u03bc"
    result["&nu;"] = "\u03bd"
    result["&xi;"] = "\u03be"
    result["&omicron;"] = "\u03bf"
    result["&pi;"] = "\u03c0"
    result["&rho;"] = "\u03c1"
    result["&sigmaf;"] = "\u03c2"
    result["&sigma;"] = "\u03c3"
    result["&tau;"] = "\u03c4"
    result["&upsilon;"] = "\u03c5"
    result["&phi;"] = "\u03c6"
    result["&chi;"] = "\u03c7"
    result["&psi;"] = "\u03c8"
    result["&omega;"] = "\u03c9"
    result["&thetasym;"] = "\u03d1"
    result["&upsih;"] = "\u03d2"
    result["&piv;"] = "\u03d6"
    result["&bull;"] = "\u2022"
    result["&hellip;"] = "\u2026"
    result["&prime;"] = "\u2032"
    result["&Prime;"] = "\u2033"
    result["&oline;"] = "\u203e"
    result["&frasl;"] = "\u2044"
    result["&weierp;"] = "\u2118"
    result["&image;"] = "\u2111"
    result["&real;"] = "\u211c"
    result["&trade;"] = "\u2122"
    result["&alefsym;"] = "\u2135"
    result["&larr;"] = "\u2190"
    result["&uarr;"] = "\u2191"
    result["&rarr;"] = "\u2192"
    result["&darr;"] = "\u2193"
    result["&harr;"] = "\u2194"
    result["&crarr;"] = "\u21b5"
    result["&lArr;"] = "\u21d0"
    result["&uArr;"] = "\u21d1"
    result["&rArr;"] = "\u21d2"
    result["&dArr;"] = "\u21d3"
    result["&hArr;"] = "\u21d4"
    result["&forall;"] = "\u2200"
    result["&part;"] = "\u2202"
    result["&exist;"] = "\u2203"
    result["&empty;"] = "\u2205"
    result["&nabla;"] = "\u2207"
    result["&isin;"] = "\u2208"
    result["&notin;"] = "\u2209"
    result["&ni;"] = "\u220b"
    result["&prod;"] = "\u220f"
    result["&sum;"] = "\u2211"
    result["&minus;"] = "\u2212"
    result["&lowast;"] = "\u2217"
    result["&radic;"] = "\u221a"
    result["&prop;"] = "\u221d"
    result["&infin;"] = "\u221e"
    result["&ang;"] = "\u2220"
    result["&and;"] = "\u2227"
    result["&or;"] = "\u2228"
    result["&cap;"] = "\u2229"
    result["&cup;"] = "\u222a"
    result["&int;"] = "\u222b"
    result["&there4;"] = "\u2234"
    result["&sim;"] = "\u223c"
    result["&cong;"] = "\u2245"
    result["&asymp;"] = "\u2248"
    result["&ne;"] = "\u2260"
    result["&equiv;"] = "\u2261"
    result["&le;"] = "\u2264"
    result["&ge;"] = "\u2265"
    result["&sub;"] = "\u2282"
    result["&sup;"] = "\u2283"
    result["&nsub;"] = "\u2284"
    result["&sube;"] = "\u2286"
    result["&supe;"] = "\u2287"
    result["&oplus;"] = "\u2295"
    result["&otimes;"] = "\u2297"
    result["&perp;"] = "\u22a5"
    result["&sdot;"] = "\u22c5"
    result["&lceil;"] = "\u2308"
    result["&rceil;"] = "\u2309"
    result["&lfloor;"] = "\u230a"
    result["&rfloor;"] = "\u230b"
    result["&lang;"] = "\u2329"
    result["&rang;"] = "\u232a"
    result["&loz;"] = "\u25ca"
    result["&spades;"] = "\u2660"
    result["&clubs;"] = "\u2663"
    result["&hearts;"] = "\u2665"
    result["&diams;"] = "\u2666"
//</editor-fold>
    result
}

