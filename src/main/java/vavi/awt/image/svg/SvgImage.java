/*
 * Copyright 1997, 1998,1999 Carmen Delessio (carmen@blackdirt.com)
 * Black Dirt Software http://www.blackdirt.com/graphics/svg
 * Free for non-commercial use
 */

package vavi.awt.image.svg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;


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
    private int svgHeight = 300;

    /** */
    private BufferedImage svgImage;

    /** */
    private Graphics2D svgGraphics;
    
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

            render(root); // put SVG image into svgImagebuffer
            
        } catch (ParserConfigurationException e) {
            throw (RuntimeException) new IllegalStateException().initCause(e);
        } catch (SAXException e) {
            throw (RuntimeException) new IllegalArgumentException().initCause(e);
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

        if (node instanceof Element) {
            Element element = (Element) node;
//System.err.println("tag: " + element.getTagName());
            if (element.getTagName().equals("text")) {
                token = element.getAttribute("x");
                int x = Integer.parseInt(token);
                token = element.getAttribute("y");
                int y = Integer.parseInt(token);
                Node textNode = node.getFirstChild();
                if (textNode.getNodeType() == Node.TEXT_NODE) {
                    svgGraphics.drawString(textNode.getNodeValue(), x, y);
                }
            } else if (element.getTagName().equals("rect")) {
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
            } else if (element.getTagName().equals("g")) {
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
//System.err.printf("color: %06x\n", currentColor);
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
//System.err.printf("color: %06x\n", currentColor);
                    svgGraphics.setColor(new Color(currentColor));
                }
                offset = token.indexOf("transform:");
                if (offset >= 0) {
                    token = token.substring(offset + 10);
                    token = token.trim();
// System.err.println ("Tranform " + tempBuffer);
                    // tempBuffer is matrix(1 0 0 -1 -174.67 414)
                    offset = token.indexOf("matrix(");
                    if (offset >= 0) {
                        token = token.substring(offset + 7);
// System.err.println ("Tranform " + tempBuffer);
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
            } else if (element.getTagName().equals("ellipse")) {
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
            } else if (element.getTagName().equals("polyline")) {
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
            } else if (element.getTagName().equals("path")) {
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
//System.err.printf("color: %06x\n", currentColor);
                    }
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
//System.err.printf("color: %06x\n", currentColor);
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
// System.out.println(tempBuffer);
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
                StringTokenizer st = new StringTokenizer(token, " ,MmLlCczArSsHhVvDdEeFfGgJjQqTtz-", true);
                while (st.hasMoreElements()) {
                    token = st.nextToken();
System.err.println("token: " + token);
// System.out.println(tempBuffer.charAt(0));
//                  boolean negate = false;
                    switch (token.charAt(0)) {
                    case 'M': // Move To
                        float fx = parseFloat(st);
                        float fy = parseFloat(st);
System.err.println("Move to: " + fx + ", "+ fy);
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
// System.out.println("fx " + fx + " fy "+ fy);
                        oldfx = fx;
                        oldfy = fy;
                        path.lineTo(fx, fy);
                        break;
                    case 'l':
                        fx = parseFloat(st);
// System.out.println("fx is " + fx);
                        fy = parseFloat(st);
// System.out.println("fy is " + fy);
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
System.err.println("Curve to: " + fx + ", "+ fy + ", " + fx1 + ", " + fy1 + ", " + fx2 + ", " + fy2);
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
System.err.println("curve to: " + fx + ", "+ fy + ", " + fx1 + ", " + fy1 + ", " + fx2 + ", " + fy2);
                                oldfx = fx2;
                                oldfy = fy2;
                            }
                        } catch (EndOfPathException e) {
                        }

                        break;

                    case 'z':
// System.err.println("closepath");
                        break;

                    case 'A':
System.err.println("Absolute");
                        break;
                    case 'r':
System.err.println("relative");
                        break;

                    case 'S':
System.err.println("Smooth curve");
                        break;

                    case 's':
System.err.println("relative smooth curve");
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
System.err.println("arc 1 - see spec");
                        break;

                    case 'd':
System.err.println("relative arc 1");
                        break;
                    case 'E':
System.err.println("arc 2 - with line");
                        break;

                    case 'e':
System.err.println("relative arc 2");
                        break;
                    case 'F':
System.err.println("arc 3");
                        break;

                    case 'f':
System.err.println("relative arc 3");
                        break;
                    case 'G':
System.err.println("arc 4");
                        break;

                    case 'g':
System.err.println("relative arc 4");
                        break;

                    case 'J':
System.err.println("elliptical quadrant");
                        break;

                    case 'j':
System.err.println("relative elliptical quadrant");
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
System.err.println("relative quadratic bezier curve to");
                        break;
                    case 'T':
System.err.println("True Type quadratic bezier curve ");
                        break;

                    case 't':
System.err.println("relative True Type quadratic bezier curve");
                        break;

                    case '-':
System.err.println("Negative value");
                        break;

                    default:
System.err.println("unknown: " + token.charAt(0));
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

            } // end if path
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
    private class EndOfPathException extends RuntimeException {
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
            floatValue = Float.parseFloat(token);
        }
        return floatValue;
    }

    /**
     * {@link #svgWidth}, {@link #svgHeight} will be set. 
     */
    private void getSize(Node node) {
        int pixelsPerInch = 0;
//System.err.println("node: " + node.getClass());
        if (node instanceof Element) {
            Element el = (Element) node;
            if (el.getTagName().equals("svg")) {
                String tempWidth = el.getAttribute("width");
                String tempHeight = el.getAttribute("height");

                pixelsPerInch = Toolkit.getDefaultToolkit().getScreenResolution();

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

                offset = tempHeight.indexOf("px");
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

                // set the width and height for component;
                // set size set = true;

System.err.println(svgWidth + ", " + svgHeight);
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

    /**
     * @param args Usage: java svgImage <source file>
     */
    public static void main(String args[]) throws Exception {
        String filename = args[0];

        final SvgImage svg = new SvgImage(new FileInputStream(filename));

        JFrame frame = new JFrame(filename);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add("Center", new JPanel() {
            public void paint(Graphics g) {
                g.drawImage(svg.getImage(), 0, 0, this);
            }
            public Dimension getPreferredSize() {
                return svg.getSize();
            }
        });
        frame.pack();
        frame.setVisible(true);
    }
}

/* */

