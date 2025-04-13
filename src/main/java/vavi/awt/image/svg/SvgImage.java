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
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_COLOR_RENDERING;
import static java.awt.RenderingHints.KEY_DITHERING;
import static java.awt.RenderingHints.KEY_FRACTIONALMETRICS;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.KEY_STROKE_CONTROL;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY;
import static java.awt.RenderingHints.VALUE_DITHER_ENABLE;
import static java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static java.awt.RenderingHints.VALUE_RENDER_QUALITY;
import static java.awt.RenderingHints.VALUE_STROKE_NORMALIZE;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
import static java.awt.geom.AffineTransform.getRotateInstance;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.System.getLogger;


/**
 * svgImage.
 *
 * @author <a href="mailto:carmen@blackdirt.com">Carmen Delessio</a>
 * @author Chris Lilley
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version May 12,1999 corrected ";" at end of color - Chris Lilley mailing list <br>
 *          May 16,1999 implemented simple Java 2d infrastructure, Begin Path <br>
 *          0.00 070630 nsano clean up <br>
 * @see "https://web.archive.org/web/20070518230947/http://www.blackdirt.com/graphics/svg"
 */
public class SvgImage {

    private static final Logger logger = getLogger(SvgImage.class.getName());

    /** */
    private boolean drawFilled;

    /** */
    private Color currentFillColor;

    /** */
    private Color currentStrokeColor;

    /** */
    private float currentStrokeWidth;

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
            dbf.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            Element root = doc.getDocumentElement();

            getSize(root);

            svgImage = new BufferedImage(svgWidth, svgHeight, TYPE_INT_ARGB);
            svgGraphics = svgImage.createGraphics();
            svgGraphics.setColor(Color.black);
            svgGraphics.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
            svgGraphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
            svgGraphics.setRenderingHint(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_QUALITY);
            svgGraphics.setRenderingHint(KEY_DITHERING, VALUE_DITHER_ENABLE);
            svgGraphics.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
            svgGraphics.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
            svgGraphics.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
            svgGraphics.setRenderingHint(KEY_FRACTIONALMETRICS, VALUE_FRACTIONALMETRICS_ON);
            svgGraphics.setRenderingHint(KEY_STROKE_CONTROL, VALUE_STROKE_NORMALIZE);

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

    /** extract value from attr or value in style */
    private static void processAttr(String string, String name, Element element,
                                    Consumer<String> exists, Runnable notExists) {
        int p = string.indexOf(name + ":"); // in style
        if (p < 0)
            string = element.getAttribute(name); // as attribute
        if (p >= 0 || !string.isEmpty()) {
            if (p >= 0)
                string = string.substring(p + name.length() + 1); // extracted from style

            exists.accept(string);
        } else {
            if (notExists != null) notExists.run();
        }
    }

    /** */
    private void render(Node node) {

        if (node instanceof Element element) {
//logger.log(Level.TRACE, "tag: <" + element.getTagName() + ">");
            switch (element.getTagName()) {
            case "text": {
                String attr = element.getAttribute("x");
                int x = Integer.parseInt(attr);
                attr = element.getAttribute("y");
                int y = Integer.parseInt(attr);
                Node textNode = node.getFirstChild();
                if (textNode.getNodeType() == Node.TEXT_NODE) {
                    svgGraphics.drawString(textNode.getNodeValue(), x, y);
                }
                break;
            }
            case "rect": {
                String attr = element.getAttribute("x");
                int x = Integer.parseInt(attr);
                attr = element.getAttribute("y");
                int y = Integer.parseInt(attr);
                attr = element.getAttribute("width");
                int w = Integer.parseInt(attr);
                attr = element.getAttribute("height");
                int h = Integer.parseInt(attr);
                if (drawFilled) {
                    svgGraphics.fillRect(x, y, w, h);
                } else {
                    svgGraphics.drawRect(x, y, w, h);
                }
                break;
            }
            case "g": {
                drawFilled = false;
                // stroke
                String style = element.getAttribute("style");
                processAttr(style, "stroke", element, attr -> {
                    currentStrokeColor = s2c(stripColorString(attr));
logger.log(Level.TRACE, String.format("g::stroke: color: #%06x", currentStrokeColor.getRGB()));
                }, () -> currentStrokeColor = null);
                // fill
                processAttr(style, "fill", element, attr -> {
                    if (attr.startsWith("none")) {
                        drawFilled = false;
                    } else {
                        currentFillColor = s2c(stripColorString(attr));
logger.log(Level.TRACE, String.format("g::fill: color: #%06x", currentFillColor.getRGB()));
                        drawFilled = true;
                    }
                }, () -> currentFillColor = null);
                // transform
                processAttr(style, "transform", element, attr -> {
                    attr = attr.trim();
//logger.log(Level.TRACE, "Transform: " + attribute);
                    // attribute is matrix(1 0 0 -1 -174.67 414)
                    int p = attr.indexOf("matrix(");
                    if (p < 0) {
logger.log(Level.WARNING, "g::transform: unrecognized: " + attr);
                        return;
                    }
                    attr = attr.substring(p + 7);
                    p = attr.indexOf(")");
                    if (p < 0) {
logger.log(Level.WARNING, "g::transform: unrecognized: " + attr);
                        return;
                    }
                    attr = attr.substring(0, p);
logger.log(Level.TRACE, "g::transform: " + attr);
                    StringTokenizer t = new StringTokenizer(attr, "- ,", true);

                    float m00 = parseFloat(t);
                    float m10 = parseFloat(t);
                    float m01 = parseFloat(t);
                    float m11 = parseFloat(t);
                    float m02 = parseFloat(t);
                    float m12 = parseFloat(t);

                    AffineTransform at = new AffineTransform(m00, m10, m01, m11, m02, m12);
                    svgGraphics.transform(at);

                    // <g id="Calque_1" style="transform:matrix(1 0 0 -1 -174.67 414);">
                }, null);
                // stroke-width
                String attr = element.getAttribute("stroke-width"); // as attribute
                if (!attr.isEmpty()) {
                    currentStrokeWidth = Float.parseFloat(attr.trim());
logger.log(Level.TRACE, "g::stroke-width: " + currentStrokeWidth);
                }
                break;
            }
            case "ellipse": {
                String attr = element.getAttribute("major");
                int major = Integer.parseInt(attr);
                attr = element.getAttribute("minor");
                int minor = Integer.parseInt(attr);
                attr = element.getAttribute("angle");
                int angle = Integer.parseInt(attr);
                attr = element.getAttribute("cx");
                int x = Integer.parseInt(attr);
                attr = element.getAttribute("cy");
                int y = Integer.parseInt(attr);

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
            case "polyline": {
                Polygon poly = new Polygon();
                String attr = element.getAttribute("verts");
                StringTokenizer t = new StringTokenizer(attr, " ,");
                while (t.hasMoreElements()) {
                    attr = t.nextToken();
                    int x = Integer.parseInt(attr);
                    attr = t.nextToken();
                    int y = Integer.parseInt(attr);
                    poly.addPoint(x, y);
                }

                if (drawFilled) {
                    svgGraphics.fillPolygon(poly);
                } else {
                    svgGraphics.drawPolyline(poly.xpoints, poly.ypoints, poly.npoints);
                }
                break;
            }
            case "path": {
                // stroke
                String style = element.getAttribute("style");
                processAttr(style, "stroke", element, attr -> {
                    currentStrokeColor = s2c(stripColorString(attr));
logger.log(Level.TRACE, String.format("path::stroke: color: #%06x", currentStrokeColor.getRGB()));
                }, null);
                // fill
                processAttr(style, "fill", element, attr -> {
logger.log(Level.TRACE, "path::fill: " + attr);
                    if (attr.startsWith("none")) {
                        drawFilled = false;
                    } else {
                        currentFillColor = s2c(stripColorString(attr));
logger.log(Level.TRACE, String.format("path::fill: color: #%06x", currentFillColor.getRGB()));
                        drawFilled = true;
                    }
                }, null);

                // d

                // Use Java2d Basics
                GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

                String attr = element.getAttribute("d");
//logger.log(Level.TRACE, "path::d::all: " + attr);

                PathDataTokenizer tokenizer = new PathDataTokenizer(attr);

                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
//logger.log(Level.TRACE, "token:d: [" + token + "]");
                    switch (token.charAt(0)) {
                    case 'M': { // Move To
                        tokenizer.loop(h -> {
                            float x = tokenizer.nextFloat();
                            float y = tokenizer.nextFloat();
logger.log(Level.TRACE, "path::d::M(Move to): " + x + ", " + y);
                            path.moveTo(x, y);
                            h.setStartPoint(x, y);
                            h.setLastPoint(x, y);
                            h.setLastKnot(x, y);
                        });
                        break;
                    }
                    case 'm': {
                        tokenizer.loop(h -> {
                            float dx = tokenizer.nextFloat();
                            float dy = tokenizer.nextFloat();
logger.log(Level.TRACE, "path::d::m(move to): " + dx + ", " + dy);
                            dx += h.x0;
                            dy += h.y0;
                            path.moveTo(dx, dy);
                            h.setStartPoint(dx, dy);
                            h.setLastPoint(dx, dy);
                            h.setLastKnot(dx, dy);
                        });
                        break;
                    }
                    case 'L': { // Line to
                        tokenizer.loop(h -> {
                            float x = tokenizer.nextFloat();
                            float y = tokenizer.nextFloat();
logger.log(Level.TRACE, "path::d::L(Line to): x " + x + " y " + y);
                            path.lineTo(x, y);
                            h.setLastPoint(x, y);
                            h.setLastKnot(x, y);
                        });
                        break;
                    }
                    case 'l': {
                        tokenizer.loop(h -> {
                            float dx = tokenizer.nextFloat();
                            float dy = tokenizer.nextFloat();
logger.log(Level.TRACE, "path::d::l(line to): " + dx + ", " + dy);
                            dx += h.x0;
                            dy += h.y0;
                            path.lineTo(dx, dy);
                            h.setLastPoint(dx, dy);
                            h.setLastKnot(dx, dy);
                        });
                        break;
                    }
                    case 'C': {
                        tokenizer.loop(h -> {
                            float x1 = tokenizer.nextFloat();
                            float y1 = tokenizer.nextFloat();
                            float x2 = tokenizer.nextFloat();
                            float y2 = tokenizer.nextFloat();
                            float x = tokenizer.nextFloat();
                            float y = tokenizer.nextFloat();
logger.log(Level.TRACE, "path::d::C(Curve to): " + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ", " + x + ", " + y);
                            path.curveTo(x1, y1, x2, y2, x, y);
                            h.setLastPoint(x, y);
                            h.setLastKnot(x2, y2);
                        });

                        // curveTo(float x1, float y1, float x2, float y2, float x3, float y3)
                        // Adds a curved segment, defined by three new points,
                        // to the path by drawing a Bézier curve
                        // that intersects both the current coordinates and the
                        // coordinates (x3, y3), using the specified points (x1, y1) and
                        // (x2, y2) as Bézier control points.

                        break;
                    }
                    case 'c': {
                        tokenizer.loop(h -> {
                            float dx1 = tokenizer.nextFloat();
                            float dy1 = tokenizer.nextFloat();
                            float dx2 = tokenizer.nextFloat();
                            float dy2 = tokenizer.nextFloat();
                            float dx = tokenizer.nextFloat();
                            float dy = tokenizer.nextFloat();
logger.log(Level.TRACE, "path::d::c(curve to): " + dx1 + ", " + dy1 + ", " + dx2 + ", " + dy2 + ", " + dx + ", " + dy);
                            dx1 += h.x0;
                            dy1 += h.y0;
                            dx2 += h.x0;
                            dy2 += h.y0;
                            dx += h.x0;
                            dy += h.y0;
                            path.curveTo(dx1, dy1, dx2, dy2, dx, dy);
                            h.setLastPoint(dx, dy);
                            h.setLastKnot(dx2, dy2);
                        });
                        break;
                    }
                    case ' ':
                        break;
                    case 'Z', 'z': {
logger.log(Level.TRACE, "path::d::z(closepath)");
                        path.closePath();
                        tokenizer.h.setLastPoint(tokenizer.h.sx, tokenizer.h.sy);
                        tokenizer.h.setLastKnot(tokenizer.h.sx, tokenizer.h.sy);
                        break;
                    }
                    case 'A': {
                        tokenizer.loop(h -> {
                            float rx = tokenizer.nextFloat();
                            float ry = tokenizer.nextFloat();
                            float xAxisRot = tokenizer.nextFloat();
                            boolean largeArc = tokenizer.nextFloat() != 0;
                            boolean sweep = tokenizer.nextFloat() != 0;
                            float x = tokenizer.nextFloat();
                            float y = tokenizer.nextFloat();
logger.log(Level.TRACE, "path::d::A(Absolute): " + rx + ", " + ry + ", " + xAxisRot + ", " + largeArc + ", " + sweep + ", " + x + ", " + y);
                            arcTo(path, rx, ry, xAxisRot, largeArc, sweep, x, y, h.x0, h.y0);
                            h.setLastPoint(x, y);
                            h.setLastKnot(x, y);
                        });
                        break;
                    }
                    case 'a': {
                        tokenizer.loop(h -> {
                            float rx = tokenizer.nextFloat();
                            float ry = tokenizer.nextFloat();
                            float xAxisRot = tokenizer.nextFloat();
                            boolean largeArc = tokenizer.nextFloat() != 0;
                            boolean sweep = tokenizer.nextFloat() != 0;
                            float dx = tokenizer.nextFloat();
                            float dy = tokenizer.nextFloat();
                            dx += h.x0;
                            dy += h.y0;
logger.log(Level.TRACE, "path::d::a(Relative): " + rx + ", " + ry + ", " + xAxisRot + ", " + largeArc + ", " + sweep + ", " + dx + ", " + dy);
                            arcTo(path, rx, ry, xAxisRot, largeArc, sweep, dx, dy, h.x0, h.y0);
                            h.setLastPoint(dx, dy);
                            h.setLastKnot(dx, dy);
                        });
                        break;
                    }
                    case 'r': {
logger.log(Level.WARNING, "path::d::r(relative)");
                        break;
                    }
                    case 'S': {
                        tokenizer.loop(h -> {
                            float x2 = tokenizer.nextFloat();
                            float y2 = tokenizer.nextFloat();
                            float x = tokenizer.nextFloat();
                            float y = tokenizer.nextFloat();
                            path.curveTo(h.x0, h.y0, x2, y2, x, y);
logger.log(Level.WARNING, "path::d::S(Smooth curve): " + x2 + ", " + y2 + ", " + x + ", " + y);
                            h.setLastPoint(x, y);
                            h.setLastKnot(x2, y2);
                        });
                        break;
                    }
                    case 's': {
                        tokenizer.loop(h -> {
                            float dx2 = tokenizer.nextFloat();
                            float dy2 = tokenizer.nextFloat();
                            float dx = tokenizer.nextFloat();
                            float dy = tokenizer.nextFloat();
                            float k1x = h.x0 * 2 - h.kx;
                            float k1y = h.y0 * 2 - h.ky;
                            dx2 += h.x0;
                            dy2 += h.y0;
                            dx += h.x0;
                            dy += h.y0;
                            path.curveTo(k1x, k1y, dx2, dy2, dx, dy);
logger.log(Level.TRACE, "path::d::s(relative smooth curve): " + dx2 + ", " + dy2 + ", " + dx + ", " + dy);
                            h.setLastPoint(dx, dy);
                            h.setLastKnot(dx2, dy2);
                        });
                        break;
                    }
                    case 'H': {
                        tokenizer.loop(h -> {
                            float x = tokenizer.nextFloat();
logger.log(Level.TRACE, "path::d::H(horizontal line): " + x);
                            path.lineTo(x, h.y0);
                            h.setLastPoint(x, h.y0);
                            h.setLastKnot(x, h.y0);
                        });
                        break;
                    }
                    case 'h': {
                        tokenizer.loop(h -> {
                            float dx = tokenizer.nextFloat();
                            dx += h.x0;
logger.log(Level.TRACE, "path::d::h(relative horizontal line): "  + dx);
                            path.lineTo(dx, h.y0);
                            h.setLastPoint(dx, h.y0);
                            h.setLastKnot(dx, h.y0);
                        });
                        break;
                    }
                    case 'V': {
                        tokenizer.loop(h -> {
                            float y = tokenizer.nextFloat();
logger.log(Level.TRACE, "path::d::V(vertical line): " + y);
                            path.lineTo(h.x0, y);
                            h.setLastPoint(h.x0, y);
                            h.setLastKnot(h.x0, y);
                        });
                        break;
                    }
                    case 'v': {
                        tokenizer.loop(h -> {
                            float dy = tokenizer.nextFloat();
                            dy += h.y0;
logger.log(Level.TRACE, "path::d::v(relative vertical line): " + dy);
                            path.lineTo(h.x0, dy);
                            h.setLastPoint(h.x0, dy);
                            h.setLastKnot(h.x0, dy);
                        });
                        break;
                    }
                    case 'D': {
logger.log(Level.WARNING, "path::d::D(arc 1 - see spec)");
                        break;
                    }
                    case 'd': {
logger.log(Level.WARNING, "path::d::d(relative arc 1)");
                        break;
                    }
                    case 'E': {
logger.log(Level.WARNING, "path::d::E(arc 2 - with line)");
                        break;
                    }
                    case 'e': {
logger.log(Level.WARNING, "path::d::e(relative arc 2)");
                        break;
                    }
                    case 'F': {
logger.log(Level.WARNING, "path::d::F(arc 3)");
                        break;
                    }
                    case 'f': {
logger.log(Level.WARNING, "path::d::f(relative arc 3)");
                        break;
                    }
                    case 'G': {
logger.log(Level.WARNING, "arc 4");
                        break;
                    }
                    case 'g': {
logger.log(Level.WARNING, "relative arc 4");
                        break;
                    }
                    case 'J': {
logger.log(Level.WARNING, "elliptical quadrant");
                        break;
                    }
                    case 'j': {
logger.log(Level.TRACE, "relative elliptical quadrant");
                        break;
                    }
                    case 'Q': {
                        tokenizer.loop(h -> {
                            float x1 = tokenizer.nextFloat();
                            float y1 = tokenizer.nextFloat();
                            float x = tokenizer.nextFloat();
                            float y = tokenizer.nextFloat();
                            path.quadTo(x1, y1, x, y);
logger.log(Level.TRACE, "path::d::Q(quadTo): " + y1 + ", " + y1 + ", " + x + ", " + y);
                            h.setLastPoint(x, y);
                            h.setLastKnot(x1, y1);
                        });
                        // quadTo(float x1, float y1, float x2, float y2)
                        // Adds a curved segment, defined by two new points,
                        // to the path by drawing a Quadratic curve that intersects
                        // both the current coordinates and the coordinates (x2, y2),
                        // using the specified point (x1, y1) as a
                        // quadratic parametric control point.

                        break;
                    }
                    case 'q': {
                        tokenizer.loop(h -> {
                            float dx1 = tokenizer.nextFloat();
                            float dy1 = tokenizer.nextFloat();
                            float dx = tokenizer.nextFloat();
                            float dy = tokenizer.nextFloat();
                            dx1 += h.x0;
                            dy1 += h.y0;
                            dx += h.x0;
                            dy += h.y0;
                            path.quadTo(dx1, dy1, dx, dy);
logger.log(Level.TRACE, "path::d::q(relative quadratic bezier curve to): " + dy1 + ", " + dy1 + ", " + dx + ", " + dy);
                            h.setLastPoint(dx, dy);
                            h.setLastKnot(dx1, dy1);
                        });
logger.log(Level.WARNING, "");
                        break;
                    }
                    case 'T': {
                        tokenizer.loop(h -> {
                            float x = tokenizer.nextFloat();
                            float y = tokenizer.nextFloat();
                            float kx = h.x0 * 2 - h.kx;
                            float ky = h.y0 * 2 - h.ky;
                            path.quadTo(kx, ky, x, y);
logger.log(Level.WARNING, "True Type quadratic bezier curve ");
                            h.setLastPoint(x, y);
                            h.setLastKnot(kx, ky);
                        });
                        break;
                    }
                    case 't': {
                        tokenizer.loop(h -> {
                            float dx = tokenizer.nextFloat();
                            float dy = tokenizer.nextFloat();
                            float kx = h.x0 * 2 - h.kx;
                            float ky = h.y0 * 2 - h.ky;
                            dx += h.x0;
                            dy += h.y0;
                            path.quadTo(kx, ky, dx, dy);
logger.log(Level.WARNING, "relative True Type quadratic bezier curve");
                            h.setLastPoint(dx, dy);
                            h.setLastKnot(kx, ky);
                        });
                        break;
                    }
                    default:
logger.log(Level.WARNING, "path::d::unknown: [" + token + "], " + attr);
                        break;
                    }
                }
                // render the path here
                BasicStroke bs = new BasicStroke(currentStrokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f);
                svgGraphics.setStroke(bs);

                if (currentFillColor != null && drawFilled) {
                    svgGraphics.setColor(currentFillColor);
                    svgGraphics.fill(path);
                }
                if (currentStrokeColor != null) {
                    svgGraphics.setColor(currentStrokeColor);
                    svgGraphics.draw(path);
                }

                currentStrokeWidth = 1;

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

    /** exception for pushback */
    private static class EndOfPathException extends RuntimeException {
        EndOfPathException() {}
        /** @param m primitive for pushback */
        EndOfPathException(String m) { super(m); }
    }

    /** float parser for path */
    private static float parseFloat(StringTokenizer st) throws EndOfPathException {
        return parseFloat(st, Float::parseFloat);
    }

    /** float parser for path */
    private static float parseFloat(StringTokenizer st, Function<String, Float> parser) throws EndOfPathException {
        float floatValue;
        if (!st.hasMoreTokens()) {
            throw new EndOfPathException();
        }
        String token = st.nextToken();
        while (token.equals(",") || token.equals(" ")) {
            token = st.nextToken();
        }
        if (token.equals("-")) {
            floatValue = -1.0f * parser.apply(st.nextToken());
        } else {
            try {
                floatValue = parser.apply(token);
            } catch (NumberFormatException e) {
//logger.log(Level.WARNING, token);
                throw new EndOfPathException(token); // for pushback
            }
        }
        return floatValue;
    }

    /**
     * {@link #svgWidth}, {@link #svgHeight} will be set.
     */
    private void getSize(Node node) {
        int pixelsPerInch = 0;
//logger.log(Level.TRACE, "node: " + node.getClass());
        if (node instanceof Element element) {
            if (element.getTagName().equals("svg")) {
                String viewBox = element.getAttribute("viewBox");
//logger.log(Level.TRACE, "viewBox: " + viewBox);
                String tempWidth;
                String tempHeight;
                if (!viewBox.isEmpty()) {
                    String[] parts = viewBox.split(" ");
                    tempWidth = parts[2];
                    tempHeight = parts[3];
                } else {
                    tempWidth = element.getAttribute("width");
                    tempHeight = element.getAttribute("height");
                }

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
                    logger.log(Level.DEBUG, "width is not found: " + svgWidth);
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
logger.log(Level.DEBUG, "height is not found: " + svgHeight);
                }

                // set the width and height for component;
                // set size set = true;

logger.log(Level.DEBUG, svgWidth + ", " + svgHeight);
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

    /** strip '#' and ';' */
    static String stripColorString(String color) {
        color = color.trim();
        if (color.startsWith("#")) {
            color = color.substring(1);
        }
        // Chris Lilley fix 5/11/99
        int semi_colon_index = color.indexOf(';');
        if (semi_colon_index > 0) {
            color = color.substring(0, semi_colon_index);
        }
        return color;
    }

    /** accepts "rrggbb", "rgb" and "black" */
    static Color s2c(String color) {
        if (color.length() == 3) {
            int r = Integer.parseInt(String.valueOf(color.charAt(0)).repeat(2), 16);
            int g = Integer.parseInt(String.valueOf(color.charAt(1)).repeat(2), 16);
            int b = Integer.parseInt(String.valueOf(color.charAt(2)).repeat(2), 16);
//logger.log(Level.TRACE, String.format("color3: %02x%02x%02x, %s", r, g, b, color);
            return new Color(r, g, b);
        } else if (color.length() == 6) {
            int r = Integer.parseInt(color.substring(0, 2), 16);
            int g = Integer.parseInt(color.substring(2, 4), 16);
            int b = Integer.parseInt(color.substring(4, 6), 16);
//logger.log(Level.TRACE, String.format("color6: %02x%02x%02x, %s", r, g, b, color);
            return new Color(r, g, b);
        } else {
            int rgb;
            if ("black".equals(color)) {
                rgb = 0;
            } else {
                rgb = Integer.parseInt(color, 16);
            }
//logger.log(Level.TRACE, String.format("color?: %06x, %s", rgb, color);
            return new Color(rgb);
        }
    }

    /** 'd' attribute in 'path' tokenizer */
    static class PathDataTokenizer extends StringTokenizer {

        // pushbacked primitive in path@d attribute
        String pushback = null;

        /** */
        PathDataTokenizer(String d) {
            super(d, " ,MmLlCczaArSsHhVvDdEeFfGgJjQqTt-", true);
        }

        @Override public boolean hasMoreTokens() {
            return this.pushback != null || super.hasMoreTokens();
        }

        @Override public String nextToken() {
            String next = this.pushback != null ? this.pushback : super.nextToken();
            this.pushback = null;
            return next;
        }

        /** */
        float nextFloat() {
            return SvgImage.parseFloat(this, this::parseFloat);
        }

        /**
         * double points recognizable
         * "1.234.567" means "1.234", "0.567"
         */
        private float parseFloat(String token) {
            int p1 = token.indexOf('.');
            int p2 = token.indexOf('.', p1 + 1);
            if (p2 != -1) {
                String first = token.substring(0, p2);
                String second = token.substring(p2);
//logger.log(Level.INFO, "second: " + second + " / " + token);
                this.pushback = second;
                return Float.parseFloat(first);
            } else {
                return Float.parseFloat(token);
            }
        }

        /** */
        static class History {

            float sx;
            float sy;
            float x0;
            float y0;
            float kx;
            float ky;

            void setStartPoint(float x, float y) {
                this.sx = x; this.sy = y;
            }

            void setLastPoint(float x, float y) {
                this.x0 = x; this.y0 = y;
            }

            void setLastKnot(float x, float y) {
                this.kx = x; this.ky = y;
            }
        }

        /** */
        History h = new History();

        /** */
        void loop(Consumer<History> loop) {
            try {
                while (true) {
                    loop.accept(h);
                }
            } catch (SvgImage.EndOfPathException e) {
//logger.log(Level.WARNING, "EndOfPathException: " + e.getMessage());
                if (e.getMessage() != null) {
                    pushback = e.getMessage();
                }
            }
        }
    }

    /**
     * Adds an elliptical arc, defined by two radii, an angle from the
     * x-axis, a flag to choose the large arc or not, a flag to
     * indicate if we increase or decrease the angles and the final
     * point of the arc.
     *
     * @param path The path that the arc will be appended to.
     *
     * @param rx the x radius of the ellipse
     * @param ry the y radius of the ellipse
     *
     * @param angle the angle from the x-axis of the current
     * coordinate system to the x-axis of the ellipse in degrees.
     *
     * @param largeArcFlag the large arc flag. If true the arc
     * spanning less than or equal to 180 degrees is chosen, otherwise
     * the arc spanning greater than 180 degrees is chosen
     *
     * @param sweepFlag the sweep flag. If true the line joining
     * center to arc sweeps through decreasing angles otherwise it
     * sweeps through increasing angles
     *
     * @param x the absolute x coordinate of the final point of the arc.
     * @param y the absolute y coordinate of the final point of the arc.
     * @param x0 - The absolute x coordinate of the initial point of the arc.
     * @param y0 - The absolute y coordinate of the initial point of the arc.
     *
     * @see "https://github.com/blackears/svgSalamander/blob/master/svg-core/src/main/java/com/kitfox/svg/pathcmd/Arc.java"
     */
    public static void arcTo(GeneralPath path, float rx, float ry,
                             float angle,
                             boolean largeArcFlag,
                             boolean sweepFlag,
                             float x, float y, float x0, float y0) {

        // Ensure radii are valid
        if (rx == 0 || ry == 0) {
            path.lineTo(x, y);
            return;
        }

        if (x0 == x && y0 == y) {
            // If the endpoints (x, y) and (x0, y0) are identical, then this
            // is equivalent to omitting the elliptical arc segment entirely.
            return;
        }

        Arc2D arc = computeArc(x0, y0, rx, ry, angle, largeArcFlag, sweepFlag, x, y);

        AffineTransform t = getRotateInstance(Math.toRadians(angle), arc.getCenterX(), arc.getCenterY());
        Shape s = t.createTransformedShape(arc);
        path.append(s, true);
    }

    /**
     * This constructs an unrotated Arc2D from the SVG specification of an
     * Elliptical arc.  To get the final arc you need to apply a rotation
     * transform such as:
     * <pre>
     * AffineTransform.getRotateInstance
     *     (angle, arc.getX()+arc.getWidth()/2, arc.getY()+arc.getHeight()/2);
     * </pre>
     *
     * @param x0 origin of arc in x
     * @param y0 origin of arc in y
     * @param rx radius of arc in x
     * @param ry radius of arc in y
     * @param angle number of radians in arc
     * @param largeArcFlag allows to choose one of the large arc (1) or small arc (0)
     * @param sweepFlag allows to choose one of the clockwise turning arc (1) or counterclockwise turning arc (0)
     * @param x ending coordinate of arc in x
     * @param y ending coordinate of arc in y
     * @return arc shape
     * @see "https://github.com/blackears/svgSalamander/blob/master/svg-core/src/main/java/com/kitfox/svg/pathcmd/Arc.java"
     */
    public static Arc2D computeArc(double x0, double y0,
                                   double rx, double ry,
                                   double angle,
                                   boolean largeArcFlag,
                                   boolean sweepFlag,
                                   double x, double y) {
        //
        // Elliptical arc implementation based on the SVG specification notes
        //

        // Compute the half distance between the current and the final point
        double dx2 = (x0 - x) / 2.0;
        double dy2 = (y0 - y) / 2.0;
        // Convert angle from degrees to radians
        angle = Math.toRadians(angle % 360.0);
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);

        //
        // Step 1 : Compute (x1, y1)
        //
        double x1 = (cosAngle * dx2 + sinAngle * dy2);
        double y1 = (-sinAngle * dx2 + cosAngle * dy2);
        // Ensure radii are large enough
        rx = Math.abs(rx);
        ry = Math.abs(ry);
        double Prx = rx * rx;
        double Pry = ry * ry;
        double Px1 = x1 * x1;
        double Py1 = y1 * y1;
        // check that radii are large enough
        double radiiCheck = Px1/Prx + Py1/Pry;
        if (radiiCheck > 1) {
            rx = Math.sqrt(radiiCheck) * rx;
            ry = Math.sqrt(radiiCheck) * ry;
            Prx = rx * rx;
            Pry = ry * ry;
        }

        //
        // Step 2 : Compute (cx1, cy1)
        //
        double sign = (largeArcFlag == sweepFlag) ? -1 : 1;
        double sq = ((Prx*Pry)-(Prx*Py1)-(Pry*Px1)) / ((Prx*Py1)+(Pry*Px1));
        sq = (sq < 0) ? 0 : sq;
        double coef = (sign * Math.sqrt(sq));
        double cx1 = coef * ((rx * y1) / ry);
        double cy1 = coef * -((ry * x1) / rx);

        //
        // Step 3 : Compute (cx, cy) from (cx1, cy1)
        //
        double sx2 = (x0 + x) / 2.0;
        double sy2 = (y0 + y) / 2.0;
        double cx = sx2 + (cosAngle * cx1 - sinAngle * cy1);
        double cy = sy2 + (sinAngle * cx1 + cosAngle * cy1);

        //
        // Step 4 : Compute the angleStart (angle1) and the angleExtent (dangle)
        //
        double ux = (x1 - cx1) / rx;
        double uy = (y1 - cy1) / ry;
        double vx = (-x1 - cx1) / rx;
        double vy = (-y1 - cy1) / ry;
        double p, n;
        // Compute the angle start
        n = Math.sqrt((ux * ux) + (uy * uy));
        p = ux; // (1 * ux) + (0 * uy)
        sign = (uy < 0) ? -1d : 1d;
        double angleStart = Math.toDegrees(sign * Math.acos(p / n));

        // Compute the angle extent
        n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
        p = ux * vx + uy * vy;
        sign = (ux * vy - uy * vx < 0) ? -1d : 1d;
        double angleExtent = Math.toDegrees(sign * Math.acos(p / n));
        if(!sweepFlag && angleExtent > 0) {
            angleExtent -= 360f;
        } else if (sweepFlag && angleExtent < 0) {
            angleExtent += 360f;
        }
        angleExtent %= 360f;
        angleStart %= 360f;

        //
        // We can now build the resulting Arc2D in double precision
        //
        Arc2D.Double arc = new Arc2D.Double();
        arc.x = cx - rx;
        arc.y = cy - ry;
        arc.width = rx * 2.0;
        arc.height = ry * 2.0;
        arc.start = -angleStart;
        arc.extent = -angleExtent;

        return arc;
    }
}
