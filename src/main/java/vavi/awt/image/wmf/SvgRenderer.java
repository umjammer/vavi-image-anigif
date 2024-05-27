/*
 * Copyright 1997, 1998,1999 Carmen Delessio (carmen@blackdirt.com)
 * Black Dirt Software http://www.blackdirt.com/graphics
 * Free for non-commercial use
 *
 * revisions:
 * May 12,1999 corrected bad URL http://www.w3.org
 * Corrected using <desc> as placeholders by embedding in their own <g>
 */

package vavi.awt.image.wmf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import vavi.awt.image.wmf.WindowsMetafile.MetaRecord;
import vavi.awt.image.wmf.WindowsMetafile.Renderer;
import vavi.io.LittleEndianDataInputStream;
import vavi.util.Debug;


/**
 * WMF Renderer for SVG.
 *
 * <li> TODO use JAXP
 * @author <a href="mailto:carmen@blackdirt.com">Carmen Delessio</a>
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070630 nsano initial version <br>
 */
class SvgRenderer implements Renderer<String> {

    /** */
    private StringBuilder svgGraphic;

    /** */
    private boolean styleSet;

    /** */
    public void init(Dimension size) {
        this.svgGraphic = new StringBuilder();

        svgGraphic.append("<?xml version = \"1.0\" standalone = \"yes\"?>\n");
        svgGraphic.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG April 1999//EN\"\n \"http://www.w3.org/Graphics/SVG/svg-19990412.dtd\">\n");
        svgGraphic.append("<svg width = \"").append(size.width).append("px\" height=\"").append(size.height).append("px\">\n");

        this.styleSet = false;
    }

    /** */
    public synchronized void render(WmfContext context, MetaRecord metaRecord, boolean fromSelect, boolean dummy) {
        try {
            StringBuilder tempBuffer;

            ByteArrayInputStream parmIn = new ByteArrayInputStream(metaRecord.getParameters());
            LittleEndianDataInputStream dis = new LittleEndianDataInputStream(parmIn);

//Debug.printf("function: 0x%04x, %d\n", mRecord.getFunction(), mRecord.getParm().length);
            switch (metaRecord.getFunction()) {

            case WindowsMetafile.META_CREATEPENINDIRECT:
                if (!fromSelect) {
                    context.addObject(context.recordIndex, metaRecord);
                } else {
                    @SuppressWarnings("unused")
                    int lbstyle = dis.readShort(); // if 5 outline is off
                    @SuppressWarnings("unused")
                    int x = dis.readShort();
                    @SuppressWarnings("unused")
                    int y = dis.readShort();
                    int selColor = dis.readInt();
                    // cvtTool.setColors(selColor);
                    context.penColor = WindowsMetafile.toColor(selColor);

                    if (styleSet) {
                        svgGraphic.append("</g> \n");
                    } else {
                        styleSet = true;
                    }
                    svgGraphic.append("<g style = \"stroke: #").append(getColorString(context.penColor)).append("\" > \n");
                }
                break;

            case WindowsMetafile.META_CREATEBRUSHINDIRECT:

                if (!fromSelect) { // if not selecting it, just add it to table
                    context.addObject(context.recordIndex, metaRecord);
                } else { // selected - use it
                    int lbstyle = dis.readShort();
                    int selColor = dis.readInt();
                    @SuppressWarnings("unused")
                    int lbhatch = dis.readShort();

                    Color color = WindowsMetafile.toColor(selColor);

                    if (styleSet) {
                        svgGraphic.append("</g> \n");
                    } else {
                        styleSet = true;
                    }

                    if (lbstyle > 0) {
                        context.drawFilled = false;
                        svgGraphic.append("<g style = \"stroke: #").append(getColorString(color)).append("\" > \n");

                    } else {
                        context.drawFilled = true; // filled
                        svgGraphic.append("<g style = \"fill: #").append(getColorString(color)).append("\" > \n");
                    }
                }
                break;

            case WindowsMetafile.META_CREATEREGION:
                if (!fromSelect) {
                    context.addObject(context.recordIndex, metaRecord);
                }
                break;

            case WindowsMetafile.META_CREATEFONTINDIRECT:
                int fontWeight = 0;
                String currentFont = "Dialog";
                if (!fromSelect) { // if not selecting it, just add it to table
                    context.addObject(context.recordIndex, metaRecord);
                } else {
                    int fontHeightShort = dis.readShort();
                    float fontHeight = fontHeightShort;
                    fontHeightShort = (int) fontHeight;
                    if (fontHeightShort < 0) {
                        fontHeightShort *= -1;
                        fontHeightShort = context.mapY(fontHeightShort);
                    } else {
                        fontHeight = (fontHeight / context.inch);
                        fontHeight = (fontHeight * 72);
                        fontHeightShort = (int) fontHeight;
                        if (fontHeightShort < 5) {
                            fontHeightShort = 9;
                        }
                    }
                    @SuppressWarnings("unused")
                    int x2 = dis.readShort(); // width
                    int y2 = dis.readShort(); // esc
                    y2 = dis.readShort(); // orientation
                    y2 = dis.readShort(); // weight
                    fontWeight = y2;
                    byte[] textBuffer = new byte[1];
                    dis.readFully(textBuffer);

                    int x = textBuffer[0]; // italic
                    boolean fontItalic = x < 0;

                    textBuffer = new byte[7];
                    dis.readFully(textBuffer);
                    tempBuffer = new StringBuilder(new String(textBuffer));

                    textBuffer = new byte[32]; // name of font
                    dis.readFully(textBuffer);
                    tempBuffer = new StringBuilder(new String(textBuffer));

                    currentFont = "Dialog";
                    if (tempBuffer.toString().startsWith("Courier")) {
                        currentFont = "Courier";
                    } else if (tempBuffer.toString().startsWith("MS Sans Serif")) {
                        currentFont = "Dialog";
                    } else if (tempBuffer.toString().startsWith("Arial")) {
                        currentFont = "Helvetica";
                    } else if (tempBuffer.toString().startsWith("Arial Narrow")) {
                        currentFont = "Helvetica";
                    } else if (tempBuffer.toString().startsWith("Arial Black")) {
                        currentFont = "Helvetica";
                        fontWeight = 700;
                    } else if (tempBuffer.toString().startsWith("Times New Roman")) {
                        currentFont = "TimesRoman";
                    } else if (tempBuffer.toString().startsWith("Wingdings")) {
                        currentFont = "ZapfDingbats";
                    }
                    @SuppressWarnings("unused")
                    int fontStyle;
                    if (fontItalic) {
                        fontStyle = Font.ITALIC;
                        if (fontWeight >= 700) { // bold + italic
                            fontStyle = 3;
                        }
                    } else {
                        fontStyle = Font.PLAIN; // plain
                        if (fontWeight >= 700) { // bold
                            fontStyle = Font.BOLD;
                        }
                    }
//                      g.setFont(new Font(currentFont, fontStyle, fontHeightShort));
                }
                svgGraphic.append("   <g>\n     <desc> Java Font definition:").append(currentFont).append(" ").append(fontWeight).append("</desc> \n   </g>\n");
                break;

            case WindowsMetafile.META_SELECTOBJECT:
                int windowInt = dis.readShort();
                metaRecord = context.selectObject(windowInt);
                render(context, metaRecord, true, false);
                break;

            case WindowsMetafile.META_DELETEOBJECT:
                windowInt = dis.readShort();
                context.deleteObject(windowInt);
                break;

            case WindowsMetafile.META_RECTANGLE:
                context.numRectangles++;
                @SuppressWarnings("unused")
                String shapeName = "rectangle" + context.numRectangles;
                int y2 = dis.readShort();
                int x2 = dis.readShort();
                int y = dis.readShort();
                int x = dis.readShort();
                x = context.mapX(x);
                x2 = context.mapX(x2);
                y = context.mapY(y);
                y2 = context.mapY(y2);
                int w = Math.abs(x2 - x);
                int h = Math.abs(y2 - y);

                // note I am doing draw-filled on a shape by shape basis,
                // SVG is more `like WMF - can do it at assignment time as a
                // style, therefore these shapes can just take care of themselves
                svgGraphic.append("   <rect x = " + "\"").append(x).append("\"").append(" y = ").append("\"").append(y).append("\"").append(" width  = ").append("\"").append(w).append("\"").append(" height = ").append("\"").append(h).append("\"").append("/>").append("\n");

                break;

            case WindowsMetafile.META_ELLIPSE:
                context.numOvals++;
                shapeName = "Oval" + context.numOvals;
                y2 = dis.readShort();
                x2 = dis.readShort();
                y = dis.readShort();
                x = dis.readShort();

                x = context.mapX(x);
                x2 = context.mapX(x2);
                y = context.mapY(y);
                y2 = context.mapY(y2);

                int major;
                int minor;
                int angle;

                w = Math.abs(x2 - x);
                h = Math.abs(y2 - y);
                if (w > h) {
                    major = w;
                    minor = h;
                    angle = 0;
                } else {
                    major = h;
                    minor = w;
                    angle = 90;
                }
                int cx = (int) (x + Math.round(0.5 * w));
                int cy = (int) (y + Math.round(0.5 * h));
                svgGraphic.append("   <ellipse cx = " + "\"").append(cx).append("\"").append(" cy = ").append("\"").append(cy).append("\"").append(" major  = ").append("\"").append(major).append("\"").append(" minor = ").append("\"").append(minor).append("\"").append(" angle = ").append("\"").append(angle).append("\"").append("/>").append("\n");
//                  svgGraphic.append(" <desc> Oval:" + + x + " " + y + " " + w + " " + h + "</desc> \n");
                break;

            case WindowsMetafile.META_POLYLINE:
                int numPoints = dis.readShort();
                // tempBuffer = " <polyline verts = \"";
                tempBuffer = new StringBuilder("   <path d = \"M");
                // read 1st point as move to segment
                x = dis.readShort();
                y = dis.readShort();
                x = context.mapX(x);
                y = context.mapY(y);
                tempBuffer.append(" ").append(x).append(",").append(y);

                for (int i = 0; i < numPoints - 1; i++) {
                    x = dis.readShort();
                    y = dis.readShort();
                    x = context.mapX(x);
                    y = context.mapY(y);
                    tempBuffer.append("L").append(x).append(",").append(y);
                }

                svgGraphic.append(tempBuffer).append("\"/> \n");
                break;

            case WindowsMetafile.META_POLYGON:
                numPoints = dis.readShort();

                // tempBuffer = " <polyline verts = \"";

                context.old.x = dis.readShort();
                context.old.y = dis.readShort();

                context.old.x = context.mapX(context.old.x);
                context.old.y = context.mapY(context.old.y);
                tempBuffer = new StringBuilder("   <path  d = \"M");
                tempBuffer.append(" ").append(context.old.x).append(",").append(context.old.y);

                for (int i = 0; i < numPoints - 1; i++) {
                    x = dis.readShort();
                    y = dis.readShort();
                    x = context.mapX(x);
                    y = context.mapY(y);
                    tempBuffer.append("L").append(x).append(",").append(y);
                }
                tempBuffer.append("L").append(context.old.x).append(",").append(context.old.y);
                svgGraphic.append(tempBuffer).append("\"/> \n");
                break;

            case WindowsMetafile.META_POLYPOLYGON:
                int numPolys = dis.readShort();

                int[] ncount = new int[numPolys];
                for (int j = 0; j < numPolys; j++) {
                    ncount[j] = dis.readShort();
                }

                for (int j = 0; j < numPolys; j++) {
                    numPoints = ncount[j];
                    tempBuffer = new StringBuilder("   <polyline verts = \"");

                    context.old.x = dis.readShort();
                    context.old.y = dis.readShort();

                    context.old.x = context.mapX(context.old.x);
                    context.old.y = context.mapY(context.old.y);

                    tempBuffer.append(" ").append(context.old.x).append(",").append(context.old.y);

                    // poly.addPoint( oldx ,oldy );

                    for (int i = 0; i < numPoints - 1; i++) {
                        x = dis.readShort();
                        y = dis.readShort();
                        x = context.mapX(x);
                        y = context.mapY(y);

                        tempBuffer.append(" ").append(x).append(",").append(y);
                    }

                    tempBuffer.append(" ").append(context.old.x).append(",").append(context.old.y);
                }

                break;

            case WindowsMetafile.META_MOVETO:
                context.old.y = dis.readShort();
                context.old.x = dis.readShort();
                context.old.x = context.mapX(context.old.x);
                context.old.y = context.mapY(context.old.y);
                svgGraphic.append("   <path  d = \" M ").append(context.old.x).append(" ").append(context.old.y).append(" \"/> \n");
                break;

            case WindowsMetafile.META_LINETO:
                context.numLines++;
                shapeName = "line" + context.numLines;
                y = dis.readShort();
                x = dis.readShort();
                x = context.mapX(x);
                y = context.mapY(y);
                svgGraphic.append("   <path  d = \" L ").append(x).append(" ").append(y).append(" \"/> \n");
                break;

            case WindowsMetafile.META_SETTEXTCOLOR:
                // save text color
                // when writing text, switch to text colors
                // when done writing, switch back

                int selColor = dis.readInt();
                context.textColor = WindowsMetafile.toColor(selColor);

                break;

            case WindowsMetafile.META_SETBKCOLOR:
                break;

            case WindowsMetafile.META_EXTTEXTOUT:
                if (styleSet) {
                    svgGraphic.append("</g> \n");
                } else {
                    styleSet = true;
                }

                svgGraphic.append("<g style = \"stroke: #").append(getColorString(context.textColor)).append("\" > \n");
                y = dis.readShort();
                x = dis.readShort();

                x = context.mapX(x);
                y = context.mapY(y);
                int numChars = dis.readShort();
                @SuppressWarnings("unused")
                int wOptions = dis.readShort();
                byte[] textBuffer = new byte[numChars];
                dis.readFully(textBuffer);
                tempBuffer = new StringBuilder(new String(textBuffer));

                svgGraphic.append("   <text x = " + "\"").append(x).append("\"").append(" y = ").append("\"").append(y).append("\" >").append(tempBuffer).append("</text>\n");
                svgGraphic.append("</g> \n");
                svgGraphic.append("<g style = \"stroke: #").append(getColorString(context.penColor)).append("\" > \n");
                break;

            case WindowsMetafile.META_TEXTOUT:
                if (styleSet) {
                    svgGraphic.append("</g> \n");
                } else {
                    styleSet = true;
                }

                svgGraphic.append("<g style = \"stroke: #").append(getColorString(context.textColor)).append("\" > \n");
                numChars = dis.readShort();
                textBuffer = new byte[numChars + 1];
                dis.readFully(textBuffer);

                tempBuffer = new StringBuilder(new String(textBuffer));

                y = dis.readShort();
                x = dis.readShort();

                x = context.mapX(x);
                y = context.mapY(y);

                svgGraphic.append("   <text x = " + "\"").append(x).append("\"").append(" y = ").append("\"").append(y).append("\" >").append(tempBuffer).append("</text>\n");
                svgGraphic.append("</g> \n");
                svgGraphic.append("<g style = \"stroke: #").append(getColorString(context.penColor)).append("\" > \n");
                break;

            case WindowsMetafile.META_STRETCHDIB:
                svgGraphic.append("  <desc> DIB - Device independent Bitmap - will convert to JPEG in next release </desc> \n");
                break;

            case WindowsMetafile.META_SETWINDOWORG:
                context.logOrg.y = dis.readShort();
                context.logOrg.x = dis.readShort();
                break;

            case WindowsMetafile.META_SETWINDOWEXT:
                context.logExt.height = dis.readShort();
                context.logExt.width = dis.readShort();
                break;

            default:
Debug.printf("unknown function: 0x%04x\n", metaRecord.getFunction());
                break;
            }

            dis.close();
        } catch (IOException e) {
            Debug.println(e);
            assert false;
        }
    }

    /** */
    public void term() {
        if (styleSet) {
            svgGraphic.append("</g>");
        }
        svgGraphic.append("</svg>\n");
// System.out.println(svgGraphic);
    }

    /** */
    public String getResult() {
        return svgGraphic.toString();
    }

    /**
     * @return rgb
     */
    private static String getColorString(Color color) {
        return String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}
