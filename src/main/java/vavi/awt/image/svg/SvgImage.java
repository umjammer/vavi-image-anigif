/*
 * Copyright 1997, 1998,1999 Carmen Delessio (carmen@blackdirt.com)
 * Black Dirt Software http://www.blackdirt.com/graphics/svg
 * Free for non-commercial use
 */

package vavi.awt.image.svg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import vavi.util.Debug;


/**
 * svgImage.
 *
 * @author <a href="mailto:carmen@blackdirt.com">Carmen Delessio</a>
 * @author Chris Lilley
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version May 12,1999 corrected ";" at end of color - Chris Lilley mailing list <br>
 *          May 16,1999 implemented simple Java 2d infrastructure, Begin Path <br>
 *          0.00 070630 nsano clean up <br>
 * @see "http://www.blackdirt.com/graphics/"
 */
public class SvgImage {

    /** */
    private boolean drawFilled;

    /** */
    private int currentColor;

    /** */
    private int svgWidth = 400;

    /** */
    private int svgHeight = 400;

    /** */
    private final BufferedImage svgImage;

    /** */
    private final Graphics2D svgGraphics;

    /** */
    public SvgImage(InputStream is) throws IOException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            Element root = doc.getDocumentElement();

            getSize(root);

            svgImage = new BufferedImage(svgWidth, svgHeight, BufferedImage.TYPE_INT_ARGB);
            svgGraphics = svgImage.createGraphics();
            svgGraphics.setColor(Color.black);
            svgGraphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            svgGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            svgGraphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            svgGraphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            svgGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            svgGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            svgGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            svgGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            svgGraphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

            render(root); // put SVG image into svgImage

        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** */
    public BufferedImage getImage() {
        return svgImage;
    }

    /** */
    public Dimension getSize() {
        return new Dimension(svgWidth, svgHeight);
    }

    /** */
    private void render(Node node) {
        String token;
        // Use Java2d Basics

        if (node instanceof Element element) {
            Debug.println(Level.FINER, "tag: " + element.getTagName());
            switch (element.getTagName()) {
            case "text": {
                token = element.getAttribute("x");
                int x = Integer.parseInt(token);
                token = element.getAttribute("y");
                int y = Integer.parseInt(token);
                Node textNode = node.getFirstChild();
                if (textNode.getNodeType() == Node.TEXT_NODE) {
                    svgGraphics.drawString(textNode.getNodeValue(), x, y);
                }
                break;
            }
            case "rect": {
                token = element.getAttribute("x");
                int x = Integer.parseInt(token);
                token = element.getAttribute("y");
                int y = Integer.parseInt(token);
                token = element.getAttribute("width");
                int w = Integer.parseInt(token);
                token = element.getAttribute("height");
                int h = Integer.parseInt(token);
                if (drawFilled) {
                    svgGraphics.fillRect(x, y, w, h);
                } else {
                    svgGraphics.drawRect(x, y, w, h);
                }
                break;
            }
            case "g": {
                token = element.getAttribute("style");
                int offset = token.indexOf("stroke:");
                if (offset >= 0) {
                    token = token.substring(offset + 7);
                    token = token.trim();
                    if (token.startsWith("#")) {
                        token = token.substring(1);
                    }
                    // Chris Lilley fix 5/11/99

                    int semi_colon_index = token.indexOf(';');
                    if (semi_colon_index > 0) {
                        token = token.substring(0, semi_colon_index);
                    }

                    // put default colors here
                    currentColor = Integer.parseInt(token, 16);
Debug.printf(Level.FINER, "color: %06x\n", currentColor);
                    svgGraphics.setColor(new Color(currentColor));
                    drawFilled = false;
                }
                offset = token.indexOf("fill:");
                if (offset >= 0) {
                    token = token.substring(offset + 5);
                    // System.out.println("Fill : " + tempBuffer);
                    token = token.trim();
                    if (token.startsWith("#")) {
                        token = token.substring(1);
                        int semi_colon_index = token.indexOf(';');
                        if (semi_colon_index > 0) {
                            token = token.substring(0, semi_colon_index);
                        }
                        currentColor = Integer.parseInt(token, 16);
                    }
                    if (token.startsWith("none")) {
                        currentColor = 0; // what does none mean?
                    }
                    drawFilled = true;
Debug.printf(Level.FINER, "color: %06x\n", currentColor);
                    svgGraphics.setColor(new Color(currentColor));
                }
                offset = token.indexOf("transform:");
                if (offset >= 0) {
                    token = token.substring(offset + 10);
                    token = token.trim();
Debug.println(Level.FINER, "Tranform " + token);
                    // tempBuffer is matrix(1 0 0 -1 -174.67 414)
                    offset = token.indexOf("matrix(");
                    if (offset >= 0) {
                        token = token.substring(offset + 7);
Debug.println(Level.FINER, "Tranform " + token);
                        StringTokenizer t = new StringTokenizer(token, " ,)");

                        token = t.nextToken();
                        float fx = Float.parseFloat(token);
                        token = t.nextToken();
                        float fy = Float.parseFloat(token);
                        token = t.nextToken();
                        float fx1 = Float.parseFloat(token);
                        token = t.nextToken();
                        float fy1 = Float.parseFloat(token);
                        token = t.nextToken();
                        float fx2 = Float.parseFloat(token);
                        token = t.nextToken();
                        float fy2 = Float.parseFloat(token);

                        @SuppressWarnings("unused")
                        AffineTransform at = new AffineTransform(fx, fy, fx1, fy1, fx2, fy2);
                    }

                    // <g id="Calque_1" style="transform:matrix(1 0 0 -1 -174.67 414);">
                }
                break;
            }
            case "ellipse": {
                token = element.getAttribute("major");
                int major = Integer.parseInt(token);
                token = element.getAttribute("minor");
                int minor = Integer.parseInt(token);
                token = element.getAttribute("angle");
                int angle = Integer.parseInt(token);
                token = element.getAttribute("cx");
                int x = Integer.parseInt(token);
                token = element.getAttribute("cy");
                int y = Integer.parseInt(token);

                int w;
                int h;
                if (angle == 90) {
                    h = major;
                    w = minor;
                } else {
                    w = major;
                    h = minor;
                }
                y = (int) (y - Math.round(0.5 * h));
                x = (int) (x - Math.round(0.5 * w));

                if (drawFilled) {
                    svgGraphics.fillOval(x, y, w, h);
                } else {
                    svgGraphics.drawOval(x, y, w, h);
                }
                break;
            }
            case "polyline":
                Polygon poly = new Polygon();
                token = element.getAttribute("verts");
                StringTokenizer t = new StringTokenizer(token, " ,");
                while (t.hasMoreElements()) {
                    token = t.nextToken();
                    int x = Integer.parseInt(token);
                    token = t.nextToken();
                    int y = Integer.parseInt(token);
                    poly.addPoint(x, y);
                }

                if (drawFilled) {
                    svgGraphics.fillPolygon(poly);
                } else {
                    svgGraphics.drawPolyline(poly.xpoints, poly.ypoints, poly.npoints);
                }
                break;
            case "path": {
                token = element.getAttribute("style");
                int offset = token.indexOf("stroke:");
                if (offset >= 0) {
                    token = token.substring(offset + 7);
                    token = token.trim();
                    if (token.startsWith("#")) {
                        token = token.substring(1);
                    }
                    // Chris Lilley fix 5/11/99
                    int semi_colon_index = token.indexOf(';');
                    if (semi_colon_index > 0) {
                        token = token.substring(0, semi_colon_index);
                    }

                    // put default colors here
                    if ("black".equals(token)) {
                        currentColor = 0x000000;
                    } else {
                        currentColor = Integer.parseInt(token, 16);
Debug.printf(Level.FINER, "color: %06x\n", currentColor);
                    }
                    svgGraphics.setColor(new Color(currentColor));
                    drawFilled = false;
                }
                offset = token.indexOf("fill:");
                if (offset >= 0) {
                    token = token.substring(offset + 5);
Debug.println(Level.FINER, "Fill : " + token);
                    token = token.trim();
                    if (token.startsWith("#")) {
                        token = token.substring(1);
                        int semi_colon_index = token.indexOf(';');
                        if (semi_colon_index > 0) {
                            token = token.substring(0, semi_colon_index);
                        }
                        currentColor = Integer.parseInt(token, 16);
                    }
                    if (token.startsWith("none")) {
                        currentColor = 0; // what does none mean?
                    }
                    drawFilled = true;
Debug.printf(Level.FINER, "color: %06x\n", currentColor);
                    svgGraphics.setColor(new Color(currentColor));
                }

//                if (offset >=0){
//                    tempBuffer = tempBuffer.substring(offset+5);
//                    tempBuffer = tempBuffer.trim();
//                    if (tempBuffer.startsWith("#")) {
//                        tempBuffer = tempBuffer.substring(1);
//                    }
//                    int semi_colon_index = tempBuffer.indexOf(';');
//                    if (semi_colon_index > 0) {
//                        tempBuffer = tempBuffer.substring(0,semi_colon_index);
//                    }
//                    currentColor = Integer.parseInt(tempBuffer,16);
//                    svgGraphics.setColor( new Color (currentColor));
//                    drawFilled = true;
//                }

                token = element.getAttribute("d");
Debug.println(Level.FINER, token);
                GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

//                 GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
//                 path.moveTo(- width / 2.0f, - height / 8.0f);
//                 path.lineTo(+ width / 2.0f, - height / 8.0f);
//                 path.lineTo(- width / 4.0f, + height / 2.0f);
//                 path.lineTo(+ 0.0f, - height / 2.0f);
//                 path.lineTo(+ width / 4.0f, + height / 2.0f);
//                 path.closePath();

                float fx2 = 0;
                float fy2 = 0;
                float oldfx = 0;
                float oldfy = 0;
                StringTokenizer st = new StringTokenizer(token, " ,MmLlCczArSsHhVvDdEeFfGgJjQqTt-", true);
                while (st.hasMoreElements()) {
                    token = st.nextToken();
Debug.println("token: " + token);
//Debug.println(Level.FINER, tempBuffer.charAt(0));
//                  boolean negate = false;
                    switch (token.charAt(0)) {
                    case 'M': // Move To
                        float fx = parseFloat(st);
                        float fy = parseFloat(st);
Debug.println("Move to: " + fx + ", " + fy);
                        oldfx = fx;
                        oldfy = fy;
                        path.moveTo(fx, fy);
                        break;
                    case 'm':
                        fx = parseFloat(st);
                        fy = parseFloat(st);
                        fx += oldfx;
                        fy += oldfy;
                        oldfx = fx;
                        oldfy = fy;
                        path.moveTo(fx, fy);
                        break;

                    case 'L': // Line to
                        fx = parseFloat(st);
                        fy = parseFloat(st);
Debug.println(Level.FINER, "fx " + fx + " fy "+ fy);
                        oldfx = fx;
                        oldfy = fy;
                        path.lineTo(fx, fy);
                        break;
                    case 'l':
                        fx = parseFloat(st);
Debug.println(Level.FINER, "fx is " + fx);
                        fy = parseFloat(st);
Debug.println(Level.FINER, "fy is " + fy);
                        fx += oldfx;
                        fy += oldfy;
                        oldfx = fx;
                        oldfy = fy;
                        path.lineTo(fx, fy);
                        break;
                    case 'C':

                        try {
                            while (true) {
                                fx = parseFloat(st);
                                fy = parseFloat(st);
                                float fx1 = parseFloat(st);
                                float fy1 = parseFloat(st);
                                fx2 = parseFloat(st);
                                fy2 = parseFloat(st);
                                path.curveTo(fx, fy, fx1, fy1, fx2, fy2);
Debug.println("Curve to: " + fx + ", " + fy + ", " + fx1 + ", " + fy1 + ", " + fx2 + ", " + fy2);
                                oldfx = fx2;
                                oldfy = fy2;
                            }
                        } catch (EndOfPathException e) {
                        }

                        // curveTo(float x1, float y1, float x2, float y2, float x3, float y3)
                        // Adds a curved segment, defined by three new points,
                        // to the path by drawing a B騷ier curve
                        // that intersects both the current coordinates and the
                        // coordinates (x3, y3), using the specified points (x1, y1) and
                        // (x2, y2) as B騷ier control points.

                        break;
                    case 'c':

                        try {
                            while (true) {
                                fx = parseFloat(st);
                                fy = parseFloat(st);
                                float fx1 = parseFloat(st);
                                float fy1 = parseFloat(st);
                                fx2 = parseFloat(st);
                                fy2 = parseFloat(st);
                                fx += oldfx;
                                fy += oldfy;
                                fx1 += oldfx;
                                fy1 = oldfy;
                                fx2 += oldfx;
                                fy2 += oldfy;
                                path.curveTo(fx, fy, fx1, fy1, fx2, fy2);
Debug.println("curve to: " + fx + ", " + fy + ", " + fx1 + ", " + fy1 + ", " + fx2 + ", " + fy2);
                                oldfx = fx2;
                                oldfy = fy2;
                            }
                        } catch (EndOfPathException e) {
                        }

                        break;

                    case 'z':
Debug.println("closepath");
                        break;

                    case 'A':
Debug.println("Absolute");
                        break;
                    case 'r':
Debug.println("relative");
                        break;

                    case 'S':
Debug.println("Smooth curve");
                        break;

                    case 's':
Debug.println("relative smooth curve");
                        break;

                    case 'H':
                        fy = parseFloat(st);
                        oldfy = fy;
                        path.lineTo(oldfx, fy);

                        break;

                    case 'h':
                        fy = parseFloat(st);
                        fy += oldfy;
                        oldfy = fy;
                        path.lineTo(oldfx, fy);

                        break;

                    case 'V':
                        fx = parseFloat(st);
                        oldfx = fx;
                        path.lineTo(fx, oldfy);

                        break;

                    case 'v':
                        fx = parseFloat(st);
                        fx += oldfx;
                        oldfx = fx;
                        path.lineTo(fx, oldfy);

                        break;

                    case 'D':
Debug.println("arc 1 - see spec");
                        break;

                    case 'd':
Debug.println("relative arc 1");
                        break;
                    case 'E':
Debug.println("arc 2 - with line");
                        break;

                    case 'e':
Debug.println("relative arc 2");
                        break;
                    case 'F':
Debug.println("arc 3");
                        break;

                    case 'f':
Debug.println("relative arc 3");
                        break;
                    case 'G':
Debug.println("arc 4");
                        break;

                    case 'g':
Debug.println("relative arc 4");
                        break;

                    case 'J':
Debug.println("elliptical quadrant");
                        break;

                    case 'j':
Debug.println("relative elliptical quadrant");
                        break;
                    case 'Q':
                        fx = parseFloat(st);
                        fy = parseFloat(st);
                        float fx1 = parseFloat(st);
                        float fy1 = parseFloat(st);
                        path.quadTo(fx, fy, fx1, fy1);
                        oldfx = fx2;
                        oldfy = fy2;

                        // quadTo(float x1, float y1, float x2, float y2)
                        // Adds a curved segment, defined by two new points,
                        // to the path by drawing a Quadratic curve that intersects
                        // both the current coordinates and the coordinates (x2, y2),
                        // using the specified point (x1, y1) as a
                        // quadratic parametric control point.

                        break;

                    case 'q':
Debug.println("relative quadratic bezier curve to");
                        break;
                    case 'T':
Debug.println("True Type quadratic bezier curve ");
                        break;

                    case 't':
Debug.println("relative True Type quadratic bezier curve");
                        break;

                    case '-':
Debug.println("Negative value");
                        break;

                    default:
Debug.println("unknown: " + token.charAt(0));
                        break;
                    }
                }
                // render the path here
                BasicStroke bs = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f);
                svgGraphics.setStroke(bs);

                if (drawFilled) {
                    svgGraphics.fill(path);
                }
                svgGraphics.draw(path);

                break;
            }
            }
        }
        if (node.hasChildNodes()) {
            NodeList nl = node.getChildNodes();
            int size = nl.getLength();
            for (int i = 0; i < size; i++) {
                render(nl.item(i));
            }
        }
    }

    /** */
    private static class EndOfPathException extends RuntimeException {
    }

    /** */
    private float parseFloat(StringTokenizer st) throws EndOfPathException {
        float floatValue;
        String token = st.nextToken();
        while (token.equals(",") || token.equals(" ")) {
            token = st.nextToken();
        }
        if (token.equals("-")) {
            floatValue = -1.0f * Float.parseFloat(st.nextToken());
        } else if ("z".equals(token)) {
            throw new EndOfPathException();
        } else {
Debug.println(token);
            floatValue = Float.parseFloat(token);
        }
        return floatValue;
    }

    /**
     * {@link #svgWidth}, {@link #svgHeight} will be set.
     */
    private void getSize(Node node) {
        int pixelsPerInch = 0;
//Debug.println("node: " + node.getClass());
        if (node instanceof Element el) {
            if (el.getTagName().equals("svg")) {
                String tempWidth = el.getAttribute("width");
                String tempHeight = el.getAttribute("height");

                pixelsPerInch = Toolkit.getDefaultToolkit().getScreenResolution();

                try {
                    int offset = tempWidth.indexOf("px");
                    if (offset >= 0) {
                        tempWidth = tempWidth.substring(0, offset);

                        svgWidth = Integer.parseInt(tempWidth);
                    } else {
                        offset = tempWidth.indexOf("inch");
                        if (offset >= 0) {
                            tempWidth = tempWidth.substring(0, offset);
                            svgWidth = Integer.parseInt(tempWidth);
                            svgWidth *= pixelsPerInch;
                        } else {
                            float width = Float.parseFloat(tempWidth);
                            svgWidth = (int) (width);
                        }
                    }
                } catch (NumberFormatException e) {
Debug.println(Level.FINE, "width is not found: " + svgWidth);
                }

                try {
                    int offset = tempHeight.indexOf("px");
                    if (offset >= 0) {
                        tempHeight = tempHeight.substring(0, offset);

                        svgHeight = Integer.parseInt(tempHeight);
                    } else {
                        offset = tempHeight.indexOf("inch");
                        if (offset >= 0) {
                            tempHeight = tempHeight.substring(0, offset);

                            svgHeight = Integer.parseInt(tempHeight);
                            svgHeight *= pixelsPerInch;
                        } else {
                            float height = Float.parseFloat(tempHeight);
                            svgHeight = (int) (height);
                        }
                    }
                } catch (NumberFormatException e) {
Debug.println(Level.FINE, "height is not found: " + svgHeight);
                }

                // set the width and height for component;
                // set size set = true;

Debug.println(Level.FINE, svgWidth + ", " + svgHeight);
                return;
            } // if it is svg
        }
        if (node.hasChildNodes()) {
            NodeList nl = node.getChildNodes();
            int size = nl.getLength();
            for (int i = 0; i < size; i++) {
                getSize(nl.item(i));
            }
        }
    }
}

