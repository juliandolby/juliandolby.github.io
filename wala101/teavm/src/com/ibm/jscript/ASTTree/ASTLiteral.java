/*
 * IBM Confidential 
 * OCO Source Materials 
 * 
 * $$FILENAME$$
 * 
 * (C) Copyright IBM Corp. 2004, 2005
 * The source code for this program is not published or otherwise 
 * divested of its trade secrets, irrespective of what has been 
 * deposited with the U. S. Copyright Office. 
 * All rights reserved.
 */
package com.ibm.jscript.ASTTree;

import java.io.PrintStream;
import java.util.GregorianCalendar;

import com.ibm.jscript.InterpretException;
import com.ibm.jscript.JScriptResources;
import com.ibm.jscript.JavaScriptException;
import com.ibm.jscript.engine.IExecutionContext;
import com.ibm.jscript.std.DateObject;
import com.ibm.jscript.types.FBSBoolean;
import com.ibm.jscript.types.FBSNull;
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSType;
import com.ibm.jscript.types.FBSUndefined;
import com.ibm.jscript.types.FBSUtility;
import com.ibm.jscript.types.FBSValue;
import com.ibm.jscript.util.DateUtilities;

public class ASTLiteral extends ASTNode {

	private FBSType type;
	private FBSValue fbsValue;

	public ASTLiteral() {
	}

	public ASTLiteral(int v) {
		this.type = FBSType.numberType;
		this.fbsValue = FBSNumber.get(v);
	}

	public ASTLiteral(long v) {
		this.type = FBSType.numberType;
		this.fbsValue = FBSNumber.get(v);
	}

	public ASTLiteral(double v) {
		this.type = FBSType.numberType;
		this.fbsValue = FBSNumber.get(v);
	}

	public ASTLiteral(boolean v) {
		this.type = FBSType.booleanType;
		this.fbsValue = FBSBoolean.get(v);
	}

	public ASTLiteral(String v) {
		this.type = FBSType.stringType;
		this.fbsValue = FBSString.get(v);
	}

	public String getTraceString() {
		return getValue().stringValue();
	}

	public FBSValue getValue() {
		return fbsValue;
	}

	private static int hexval(char c) {
		switch (c) {
		case '0':
			return 0;
		case '1':
			return 1;
		case '2':
			return 2;
		case '3':
			return 3;
		case '4':
			return 4;
		case '5':
			return 5;
		case '6':
			return 6;
		case '7':
			return 7;
		case '8':
			return 8;
		case '9':
			return 9;

		case 'a':
		case 'A':
			return 10;
		case 'b':
		case 'B':
			return 11;
		case 'c':
		case 'C':
			return 12;
		case 'd':
		case 'D':
			return 13;
		case 'e':
		case 'E':
			return 14;
		case 'f':
		case 'F':
			return 15;
		}
		return -1;// must never reach this point
	}

	private static int octval(char c) {
		switch (c) {
		case '0':
			return 0;
		case '1':
			return 1;
		case '2':
			return 2;
		case '3':
			return 3;
		case '4':
			return 4;
		case '5':
			return 5;
		case '6':
			return 6;
		case '7':
			return 7;
		case '8':
			return 8;
		case '9':
			return 9;

		case 'a':
		case 'A':
			return 10;
		case 'b':
		case 'B':
			return 11;
		case 'c':
		case 'C':
			return 12;
		case 'd':
		case 'D':
			return 13;
		case 'e':
		case 'E':
			return 14;
		case 'f':
		case 'F':
			return 15;
		}
		return -1;// must never reach this point
	}

	public void setStringValue(String image) {
		this.type = FBSType.stringType;
		
		if ((image.startsWith("\"") && image.endsWith("\"")) //$NON-NLS-1$ //$NON-NLS-2$
				|| (image.startsWith("'") && image.endsWith("'"))) { //$NON-NLS-1$ //$NON-NLS-2$
			image = image.substring(1, image.length() - 1);
		}
		int l = image.length();
		StringBuffer sb = new StringBuffer(l);
		for (int i = 0; i < l; i++) {
			char c = image.charAt(i);
			if ((c == '\\') && (i + 1 < l)) {
				i++;
				c = image.charAt(i);
				if (c == 'n')
					c = '\n';
				else if (c == 'b')
					c = '\b';
				else if (c == 'f')
					c = '\f';
				else if (c == 'r')
					c = '\r';
				else if (c == 't')
					c = '\t';
				else if (c == 'x') {
					c = (char) (hexval(image.charAt(i + 1)) << 4 | hexval(image
							.charAt(i + 1)));
					i += 2;
				} else if (c == 'u') {
					c = (char) (hexval(image.charAt(i + 1)) << 12
							| hexval(image.charAt(i + 2)) << 8
							| hexval(image.charAt(i + 3)) << 4 | hexval(image
							.charAt(i + 4)));
					i += 4;
				} else if (c >= '0' && c <= '7') {
					c = (char) (octval(image.charAt(i)));
					if ((image.length() > i) && (image.charAt(i + 1) >= '0')
							&& (image.charAt(i + 1) <= '7')) {
						i++;
						c = (char) ((c << 4) | octval(image.charAt(i)));
					}
				}
			}
			sb.append(c);
		}
		fbsValue = FBSString.get(sb.toString());
	}

	public void setDecimalValue(String image) {
		this.type = FBSType.numberType;
		this.fbsValue = FBSNumber.get(image);
	}

	public void setOctalValue(String image) {
		this.type = FBSType.numberType;
		try {
			String imageWithout0 = image.substring(1);
			this.fbsValue = FBSNumber.get(Long.parseLong(imageWithout0, 8));
		} catch (NumberFormatException e) {
			fbsValue = FBSNumber.get(image);
		}
	}

	public void setHexValue(String image) {
		this.type = FBSType.numberType;
		try {
			String imageWithout0x = image.substring(2);
			this.fbsValue = FBSNumber.get(Long.parseLong(imageWithout0x, 16));
		} catch (NumberFormatException e) {
			fbsValue = FBSNumber.get(image);
		}
	}

	public void setBooleanValue(boolean value) {
		this.type = FBSType.booleanType;
		this.fbsValue = FBSBoolean.get(value);
	}

	public void setDateValue(String image)
			throws com.ibm.jscript.parser.ParseException {
		this.type = FBSType.dateType;
		try {
			DateParser dp = new DateParser();
			this.fbsValue = FBSUtility.wrap(new DateObject(dp.parseDate(image)));
		} catch (InterpretException e) {
		}
	}

	public static long parseDate(String image) throws InterpretException {
		DateParser dp = new DateParser();
		try {
			return dp.parseDate(image);
		} catch (com.ibm.jscript.parser.ParseException e) {
			throw new InterpretException(JScriptResources.getString("ASTLiteral.IllegalDate.Exception")); //$NON-NLS-1$
		}
	}

	static class DateParser {
		int pos = 0;

		StringBuffer buf;
		char dateSep = '0';
		char timeSep = '0';
		static final String dateSeparators = ";,./- \t"; //$NON-NLS-1$
		static final String timeSeparators = ":. \t"; //$NON-NLS-1$
		static final String dateTimeSeparators = "- Tt"; //$NON-NLS-1$

		public long parseDate(String image)
				throws com.ibm.jscript.parser.ParseException {
			buf = new StringBuffer(image);
			// strip #
            // strip # if present
            if (buf.length()>1) {
                if(!Character.isDigit(buf.charAt(0))) {
			buf.deleteCharAt(0);
                }
                if(!Character.isDigit(buf.charAt(buf.length() - 1))) {
			buf.deleteCharAt(buf.length() - 1);
                }
            }
			int year = 2000;
			int month = 0;
			int day = 1;
			int hour = 0;
			int min = 0;
			int sec = 0;

			int sz = buf.length();
			skipSpace();
			year = getYear();
			if (year <= 99 && year >= 40) {
				year += 1900;
			} else if (year < 40) {
				year += 2000;
			}
			if (pos < sz) {// 1
				getSep(0);
                // the month in java Date is between 0 and 11
                month=get2Digit()-1;
                if (pos<sz){// 2
					getSep(0);
					day = get2Digit();
					if (pos < sz) {// 3
						getSep(1);
						hour = get2Digit();
						if (pos < sz) {// 4
							getSep(2);
							min = get2Digit();
							if (pos < sz) {// 5
								getSep(2);
								sec = get2Digit();
							}//5
						}// 4
					}// 3
				}// 2
			}// 1
			DateUtilities.TGregorianCalendar c = DateUtilities.getCalendar();
			try {
				c.set(year, month, day, hour, min, sec);
				c.set(GregorianCalendar.MILLISECOND, 0);
				return c.getMillis();
			} finally {
				com.ibm.jscript.util.DateUtilities.recycleCalendar(c);
			}
		}

		private void getSep(int sepType)
				throws com.ibm.jscript.parser.ParseException {
			skipSpace();
			char c = buf.charAt(pos);
			if (Character.isDigit(c)) {
				c = ' ';
			} else {
				pos++;
			}

			if (sepType == 0) {//date separator
				if (dateSeparators.indexOf(c) < 0) {
					throw new com.ibm.jscript.parser.ParseException(
							JScriptResources.getString("ASTLiteral.IllegalSeparator.Exception")); //$NON-NLS-1$
				}
				if (dateSep == '0') {
					dateSep = c;
				} else if (dateSep != c) {
					throw new com.ibm.jscript.parser.ParseException(
							JScriptResources.getString("ASTLiteral.ParseDateError.Exception")); //$NON-NLS-1$
				}
			} else if (sepType == 1) {//dateTime separator
				if (dateTimeSeparators.indexOf(c) < 0) {
					throw new com.ibm.jscript.parser.ParseException(
							JScriptResources.getString("ASTLiteral.IllegalSeparator.Exception")); //$NON-NLS-1$
				}
			} else if (sepType == 2) {//time separator
				if (timeSeparators.indexOf(c) < 0) {
					throw new com.ibm.jscript.parser.ParseException(
							JScriptResources.getString("ASTLiteral.IllegalSeparator.Exception")); //$NON-NLS-1$
				}
				if (timeSep == '0') {
					timeSep = c;
				} else if (timeSep != c) {
					throw new com.ibm.jscript.parser.ParseException(
							JScriptResources.getString("ASTLiteral.ParseTimeError.Exception")); //$NON-NLS-1$
				}
			}
			skipSpace();
		}

		private void skipSpace() {
			int sz = buf.length();
			while (pos < sz) {
				char c = buf.charAt(pos);
				if (!Character.isSpaceChar(c)) {
					return;
				}
				pos++;
			}
		}

		private int getYear() throws com.ibm.jscript.parser.ParseException {
			int cnt = 0;
			int year = 0;
			int sz = buf.length();
			while (cnt <= 4 && pos < sz) {
				cnt++;
				char c = buf.charAt(pos);
				if (!Character.isDigit(c)) {
					return year;
				}
				year = (year * 10) + Character.getNumericValue(c);
				pos++;
			}
			if (pos >= sz)
				return year;
			throw new com.ibm.jscript.parser.ParseException(JScriptResources.getString("ASTLiteral.IllegalYear.Exception")); //$NON-NLS-1$
		}

		private int get2Digit() throws com.ibm.jscript.parser.ParseException {
			int cnt = 0;
			int val = 0;
			int sz = buf.length();
			while (cnt <= 2 && pos < sz) {
				cnt++;
				char c = buf.charAt(pos);
				if (!Character.isDigit(c)) {
					return val;
				}
				val = (val * 10) + Character.getNumericValue(c);
				pos++;
			}
			if (pos >= sz)
				return val;
			throw new com.ibm.jscript.parser.ParseException(JScriptResources.getString("ASTLiteral.IllegalDate.Exception")); //$NON-NLS-1$
		}

	}

	public void setNullValue() {
		this.type = FBSType.undefinedType;
		this.fbsValue = FBSNull.nullValue;
	}

	public void setUndefinedValue() {
		this.type = FBSType.undefinedType;
		this.fbsValue = FBSUndefined.undefinedValue;
	}

	public int getSlotCount() {
		return 0;
	}

	public ASTNode readSlotAt(int index) {
		return null;
	}

	public String toString() {
		return fbsValue.stringValue();
	}

	public void interpret(ASTNode caller, IExecutionContext context,
			InterpretResult result) throws JavaScriptException {
		result.setNormal(fbsValue);
	}

	public FBSType getResultType(FBSType.ISymbolCallback symbolCallback) {
		return type;
	}

	public boolean isConstant() {
		return true;
	}

	public FBSValue evaluateConstant() throws InterpretException {
		return fbsValue;
	}

	protected void extraInfo(PrintStream ps, String indent) {
		ps.print("value="); //$NON-NLS-1$
		ps.print(fbsValue.stringValue());
	}

    public Object visit(ASTNodeVisitor v, Object param) {
	return v.visitLiteral(this, param);
    }
}
