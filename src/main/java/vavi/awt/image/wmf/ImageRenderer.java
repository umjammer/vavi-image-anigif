/*
 * Copyright 1997, 1998 Carmen Delessio (carmen@blackdirt.com)
 * Black Dirt Software http://www.blackdirt.com/graphics
 * Free for non-commercial use
 */

package vavi.awt.image.wmf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;

import vavi.awt.image.wmf.WindowsMetafile.MetaRecord;
import vavi.awt.image.wmf.WindowsMetafile.Renderer;
import vavi.io.LittleEndianDataInputStream;
import vavi.util.Debug;


/**
 * WMF Renderer for Java Image.
 *
 * @author <a href="mailto:carmen@blackdirt.com">Carmen Delessio</a>
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070630 nsano initial version <br>
 * @see "http://www.blackdirt.com/graphics/"
 */
class ImageRenderer implements Renderer<Image> {

    /** */
    private Image wmfImage;
    /** */
    private Graphics wmfGraphics;

    /** */
    private StringBuilder javaGraphic;
    /** */
    private StringBuilder javaDeclare;

    /** */
    public void init(Dimension size) {

        this.wmfImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        this.wmfGraphics = wmfImage.getGraphics();

        this.javaGraphic = new StringBuilder();
        this.javaDeclare = new StringBuilder();
    }

    /** */
    public void render(WmfContext context, MetaRecord metaRecord, boolean fromSelect, boolean play) {

        if (metaRecord == null) {
            return;
        }

        try {
            String tempBuffer;

            ByteArrayInputStream bais = new ByteArrayInputStream(metaRecord.getParameters());
            LittleEndianDataInputStream dis = new LittleEndianDataInputStream(bais);

Debug.printf(Level.FINE, "function: 0x%04x, %d\n", metaRecord.getFunction(), metaRecord.getParameters().length);
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
                    context.penColor = WindowsMetafile.toColor(selColor);
Debug.printf(Level.FINE, "0x%04x: color: %s\n", metaRecord.getFunction(), context.penColor);
                    wmfGraphics.setColor(context.penColor);
                }
                break;

            case WindowsMetafile.META_CREATEREGION:
                if (!fromSelect) {
                    context.addObject(context.recordIndex, metaRecord);
                }
                break;

            case WindowsMetafile.META_CREATEFONTINDIRECT:
                if (!fromSelect) { // if not selecting it, just add it to table
                    context.addObject(context.recordIndex, metaRecord);
                } else {
                    int fontHeightInt = dis.readShort();
                    float fontHeight = fontHeightInt;
                    fontHeightInt = (int) fontHeight;
                    if (fontHeightInt < 0) {
                        fontHeightInt *= -1;
                        fontHeightInt = context.mapY(fontHeightInt);
                    } else {
                        fontHeight = (fontHeight / context.inch);
                        fontHeight = (fontHeight * 72);
                        fontHeightInt = (int) fontHeight;
                        if (fontHeightInt < 5) {
                            fontHeightInt = 9;
                        }
                    }
                    @SuppressWarnings("unused")
                    int x2 = dis.readShort(); // width
                    int y2 = dis.readShort(); // esc
                    y2 = dis.readShort(); // orientation
                    y2 = dis.readShort(); // weight
                    int fontWeight = y2;
                    byte[] textBuffer = new byte[1];
                    dis.readFully(textBuffer);

                    int x = textBuffer[0]; // italic
                    boolean fontItalic = x < 0;

                    textBuffer = new byte[7];
                    dis.readFully(textBuffer);
                    tempBuffer = new String(textBuffer);

                    textBuffer = new byte[32]; // name of font
                    dis.readFully(textBuffer);
                    tempBuffer = new String(textBuffer);

                    String currentFont = "Dialog";
                    if (tempBuffer.startsWith("Courier")) {
                        currentFont = "Courier";
                    } else if (tempBuffer.startsWith("MS Sans Serif")) {
                        currentFont = "Dialog";
                    } else if (tempBuffer.startsWith("Arial")) {
                        currentFont = "Helvetica";
                    } else if (tempBuffer.startsWith("Arial Narrow")) {
                        currentFont = "Helvetica";
                    } else if (tempBuffer.startsWith("Arial Black")) {
                        currentFont = "Helvetica";
                        fontWeight = 700;
                    } else if (tempBuffer.startsWith("Times New Roman")) {
                        currentFont = "TimesRoman";
                    } else if (tempBuffer.startsWith("Wingdings")) {
                        currentFont = "ZapfDingbats";
                    }
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
                    wmfGraphics.setFont(new Font(currentFont, fontStyle, fontHeightInt));
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
                    // filled
                    context.drawFilled = lbstyle <= 0;
                    Color c = WindowsMetafile.toColor(selColor);
Debug.printf(Level.FINE, "0x%04x: color: %s\n", metaRecord.getFunction(), c);
                    if (play) {
                        wmfGraphics.setColor(c);
                    } else {
                        javaGraphic.append("    g.setColor( new Color( ").append(c.getRed()).append(",").append(c.getGreen()).append(",").append(c.getBlue()).append("));").append("\n");
                    }
                }
                break;

            case WindowsMetafile.META_SELECTOBJECT:
                int index = dis.readShort();
                metaRecord = context.selectObject(index);
                render(context, metaRecord, true, play);
                break;

            case WindowsMetafile.META_DELETEOBJECT:
                index = dis.readShort();
                context.deleteObject(index);
                break;

            case WindowsMetafile.META_RECTANGLE:
                context.numRectangles++;
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

                tempBuffer = "( " + x + ", " + y + ", " + w + ", " + h + "); // rectangle";
                if (context.drawFilled) {
                    wmfGraphics.fillRect(x, y, w, h);
                } else {
                    wmfGraphics.drawRect(x, y, w, h);
                }

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

                w = Math.abs(x2 - x);
                h = Math.abs(y2 - y);

                tempBuffer = "( " + x + ", " + y + ", " + w + ", " + h + ");//  rectangle";
                if (context.drawFilled) {
                    wmfGraphics.fillOval(x, y, w, h);
                } else {
                    wmfGraphics.drawOval(x, y, w, h);
                }

                break;

            case WindowsMetafile.META_POLYLINE:
                Polygon poly = new Polygon();
                int numPoints = dis.readShort();

                for (int i = 0; i < numPoints; i++) {
                    x = dis.readShort();
                    y = dis.readShort();
                    x = context.mapX(x);
                    y = context.mapY(y);
                    poly.addPoint(x, y);
                }
                wmfGraphics.drawPolygon(poly);
                break;

            case WindowsMetafile.META_POLYGON:
                poly = new Polygon();
                numPoints = dis.readShort();

                context.old.x = dis.readShort();
                context.old.y = dis.readShort();

                context.old.x = context.mapX(context.old.x);
                context.old.y = context.mapY(context.old.y);

                poly.addPoint(context.old.x, context.old.y);
                for (int i = 0; i < numPoints - 1; i++) {
                    x = dis.readShort();
                    y = dis.readShort();
                    x = context.mapX(x);
                    y = context.mapY(y);
                    poly.addPoint(x, y);
                }
                poly.addPoint(context.old.x, context.old.y);

Debug.println(Level.FINE, "color: " + wmfGraphics.getColor());
                if (context.drawFilled) {
                    wmfGraphics.fillPolygon(poly);
                } else {
                    wmfGraphics.drawPolygon(poly);
                }
                break;

            case WindowsMetafile.META_POLYPOLYGON:
                int numPolys = dis.readShort();

                int[] ncount = new int[numPolys];
                for (int j = 0; j < numPolys; j++) {
                    ncount[j] = dis.readShort();
                }

                for (int j = 0; j < numPolys; j++) {
                    poly = new Polygon();

                    numPoints = ncount[j];

                    context.old.x = dis.readShort();
                    context.old.y = dis.readShort();

                    context.old.x = context.mapX(context.old.x);
                    context.old.y = context.mapY(context.old.y);

                    poly.addPoint(context.old.x, context.old.y);

                    for (int i = 0; i < numPoints - 1; i++) {
                        x = dis.readShort();
                        y = dis.readShort();

                        x = context.mapX(x);
                        y = context.mapY(y);

                        poly.addPoint(x, y);
                    }

                    poly.addPoint(context.old.x, context.old.y);

                    if (context.drawFilled) {
                        wmfGraphics.fillPolygon(poly);
                    } else {
                        wmfGraphics.drawPolygon(poly);
                    }
                }

                break;

            case WindowsMetafile.META_MOVETO:
                context.old.y = dis.readShort();
                context.old.x = dis.readShort();
                context.old.x = context.mapX(context.old.x);
                context.old.y = context.mapY(context.old.y);
                break;

            case WindowsMetafile.META_LINETO:
                context.numLines++;
                shapeName = "line" + context.numLines;
                javaDeclare.append("    Polygon ").append(shapeName).append("= new Polygon();").append("\n");
                y = dis.readShort();
                x = dis.readShort();
                x = context.mapX(x);
                y = context.mapY(y);

                wmfGraphics.drawLine(context.old.x, context.old.y, x, y);

                break;

            case WindowsMetafile.META_SETTEXTCOLOR:
                // save text color
                // when writing text, switch to text colors
                // when done writing, switch back

                int selColor = dis.readInt();
                context.textColor = WindowsMetafile.toColor(selColor);

Debug.printf(Level.FINE, "0x%04x: color: %s\n", metaRecord.getFunction(), context.textColor);
                wmfGraphics.setColor(context.textColor);
                break;

            case WindowsMetafile.META_SETBKCOLOR:
Debug.printf(Level.FINE, "0x%04x: color: %s\n", metaRecord.getFunction(), context.penColor);
                wmfGraphics.setColor(context.penColor);
                break;

            case WindowsMetafile.META_EXTTEXTOUT:
Debug.printf(Level.FINE, "0x%04x: color: %s\n", metaRecord.getFunction(), context.textColor);
                wmfGraphics.setColor(context.textColor);

                y = dis.readShort();
                x = dis.readShort();

                x = context.mapX(x);
                y = context.mapY(y);
                int numChars = dis.readShort();
                @SuppressWarnings("unused")
                int wOptions = dis.readShort();
                byte[] textBuffer = new byte[numChars];
                dis.readFully(textBuffer);
                tempBuffer = new String(textBuffer);
                wmfGraphics.drawString(tempBuffer, x, y);
Debug.printf(Level.FINE, "0x%04x: color: %s\n", metaRecord.getFunction(), context.penColor);
                wmfGraphics.setColor(context.penColor);
                break;

            case WindowsMetafile.META_TEXTOUT:
Debug.printf(Level.FINE, "0x%04x: color: %s\n", metaRecord.getFunction(), context.textColor);
                wmfGraphics.setColor(context.textColor);
                numChars = dis.readShort();
                textBuffer = new byte[numChars + 1];
                dis.readFully(textBuffer);

                tempBuffer = new String(textBuffer);

                y = dis.readShort();
                x = dis.readShort();

                x = context.mapX(x);
                y = context.mapY(y);
                wmfGraphics.drawString(tempBuffer, x, y);
Debug.printf(Level.FINE, "0x%04x: color: %s\n", metaRecord.getFunction(), context.penColor);
                wmfGraphics.setColor(context.penColor);

                break;

            case WindowsMetafile.META_STRETCHDIB:
                Image image;
                BmpImage bmp = null;
                tempBuffer = new String(metaRecord.getParameters());
                tempBuffer = tempBuffer.substring(22);
                bmp = new BmpImage(tempBuffer, 1);
Debug.println(Level.FINE, " instantiated");
                image = bmp.getImage();
                wmfGraphics.drawImage(image, 0, 0, null);
                break;

            case WindowsMetafile.META_SETWINDOWORG:
                context.logOrg.x = dis.readShort();
                context.logOrg.y = dis.readShort();
                break;

            case WindowsMetafile.META_SETWINDOWEXT:
                context.logExt.height = dis.readShort();
                context.logExt.width = dis.readShort();
                break;

            default:
Debug.printf(Level.FINE, "unknown function: 0x%04x\n", metaRecord.getFunction());
                javaGraphic.append("// unrecognized function ").append(metaRecord.getFunction()).append("\n");
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
    }

    /** */
    public Image getResult() {
        return wmfImage;
    }
}
