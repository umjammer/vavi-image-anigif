/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.wmf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import vavi.io.LittleEndianDataInputStream;
import vavi.util.Debug;


/**
 * WindowsMetafile.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070702 nsano initial version <br>
 * @see "http://www.blackdirt.com/graphics/"
 */
public class WindowsMetafile {

    static final int META_SETBKCOLOR = 0x0201;
    static final int META_SETBKMODE = 0x0102;
    static final int META_SETMAPMODE = 0x0103;
    static final int META_SETROP2 = 0x0104;
    static final int META_SETPOLYFILLMODE = 0x0106;
    static final int META_SETSTRETCHBLTMODE = 0x0107;
    static final int META_SETTEXTCOLOR = 0x0209;
    static final int META_SETTEXTCHAREXTRA = 0x0108;
    static final int META_SETWINDOWORG = 0x020b;
    static final int META_SETWINDOWEXT = 0x020c;
    static final int META_SETVIEWPORTORG = 0x020d;
    static final int META_SETVIEWPORTEXT = 0x020e;
    static final int META_OFFSETWINDOWORG = 0x020f;
    static final int META_SCALEWINDOWEXT = 0x0410;
    static final int META_OFFSETVIEWPORTORG = 0x0211;
    static final int META_SCALEVIEWPORTEXT = 0x0412;
    static final int META_LINETO = 0x0213;
    static final int META_MOVETO = 0x0214;
    static final int META_EXCLUDECLIPRECT = 0x0415;
    static final int META_INTERSECTCLIPRECT = 0x0416;
    static final int META_ARC = 0x0817;
    static final int META_ELLIPSE = 0x0418;
    static final int META_FLOODFILL = 0x0419;
    static final int META_PIE = 0x081a;
    static final int META_RECTANGLE = 0x041b;
    static final int META_ROUNDRECT = 0x061c;
    static final int META_PATBLT = 0x061d;
    static final int META_SAVEDC = 0x001e;
    static final int META_SETPIXEL = 0x041f;
    static final int META_OFFSETCLIPRGN = 0x0220;
    static final int META_POLYGON = 0x0324;
    static final int META_POLYLINE = 0x0325;
    static final int META_ESCAPE = 0x0626;
    static final int META_RESTOREDC = 0x0127;
    static final int META_FILLREGION = 0x0228;
    static final int META_FRAMEREGION = 0x0429;
    static final int META_INVERTREGION = 0x012a;
    static final int META_PAINTREGION = 0x012b;
    static final int META_SELECTCLIPREGION = 0x012c;
    static final int META_SELECTOBJECT = 0x012d;
    static final int META_SETTEXTALIGN = 0x012e;
    static final int META_CHORD = 0x0830;
    static final int META_SETMAPPERFLAGS = 0x0231;
    static final int META_TEXTOUT = 0x0521;
    static final int META_EXTTEXTOUT = 0x0a32;
    static final int META_SETDIBTODEV = 0x0d33;
    static final int META_POLYPOLYGON = 0x0538;
    static final int META_DIBBITBLT = 0x0940;
    static final int META_DIBSTRETCHBLT = 0x0b41;
    static final int META_EXTFLOODFILL = 0x0548;
    static final int META_DELETEOBJECT = 0x01f0;
    static final int META_CREATEPENINDIRECT = 0x02fa;
    static final int META_CREATEFONTINDIRECT = 0x02fb;
    static final int META_CREATEBRUSHINDIRECT = 0x02fc;
    static final int META_CREATEREGION = 0x06ff;
    static final int META_STRETCHDIB = 0x0f43;
    static final int META_SETTEXTJUSTIFICATION = 0x020a;
    static final int META_BITBLT = 0x0922;
    static final int META_STRETCHBLT = 0x0b23;
    static final int META_CREATEPATTERNBRUSH = 0x01f9;
    static final int META_SELECTPALETTE = 0x0234;
    static final int META_REALIZEPALETTE = 0x0035;
    static final int META_ANIMATEPALETTE = 0x0436;
    static final int META_SETPALENTRIES = 0x0037;
    static final int META_RESIZEPALETTE = 0x0139;
    static final int META_CREATEPALETTE = 0x00f7;
    static final int META_SETRELABS = 0x0105;

    /** */
    static class SpecialHeader {
        /** Magic number (always 9AC6CDD7h) */
        int key;
        /** Metafile HANDLE number (always 0) */
        int handle;
        /** Left coordinate in metafile units */
        int left;
        /** Top coordinate in metafile units */
        int top;
        /** Right coordinate in metafile units */
        int right;
        /** Bottom coordinate in metafile units */
        int bottom;
        /** Number of metafile units per inch */
        int inch;
        /** Reserved (always 0) */
        int reserved;
        /** Checksum value for previous 10 WORDs */
        int checksum;
        /** */
        public static SpecialHeader readFrom(InputStream is) throws IOException {
            SpecialHeader header = new SpecialHeader();

            LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);

            header.key = dis.readInt(); // key 4 bytes
//Debug.printf("0x%08x\n", header.key);
            header.handle = dis.readShort();
            header.left = dis.readShort();
            header.top = dis.readShort();
            header.right = dis.readShort();
            header.bottom = dis.readShort();
            header.inch = dis.readShort();
            header.reserved = dis.readInt();
            header.checksum = dis.readShort();

            return header;
        }
    }

    /** */
    static class MetaHeader {
        /** Type of metafile (1=memory, 2=disk) */
        int fileType;
        /** Size of header in WORDS (always 9) */
        int headerSize;
        /** Version of Microsoft Windows used */
        int version;
        /** Total size of the metafile in WORDs */
        int fileSize;
        /** Number of objects in the file */
        int numOfObjects;
        /** The size of largest record in WORDs */
        int maxRecordSize;
        /** Not Used (always 0) */
        int noParameters;
        /** */
        public static MetaHeader readFrom(InputStream is) throws IOException {
            MetaHeader header = new MetaHeader();

            LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);

            header.fileType = dis.readShort();
            header.headerSize = dis.readShort();
            header.version = dis.readShort();
            header.fileSize = dis.readInt();
            header.numOfObjects = dis.readShort();
            header.maxRecordSize = dis.readInt();
            header.noParameters = dis.readShort();

            return header;
        }
    }

    /** */
    static class MetaRecord {

        /** Total size of the record in WORDs */
        private final int size;
        /** Function number (defined in WINDOWS.H) */
        private final int function;
        /** Parameter values passed to function */
        private final byte[] parameters;

        /** */
        public MetaRecord(int size, int function, byte[] parameters) {
            this.size = size;
            this.function = function;
            this.parameters = parameters;
        }

        /** */
        public int getSize() {
            return size;
        }

        /** */
        public int getFunction() {
            return function;
        }

        /** */
        public byte[] getParameters() {
            return parameters;
        }

        /** */
        public static MetaRecord readFrom(InputStream is) throws IOException {

            LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);

            int size = dis.readInt();
            int function = dis.readShort();

            if (size >= 3) {
                size = (size * 2) - 6;
            }

            byte[] parmeters = new byte[size];
            dis.readFully(parmeters);
            MetaRecord metaRecord = new MetaRecord(size, function, parmeters);

            return metaRecord;
        }
    }

    /** */
    private SpecialHeader specialHeader;

    /** */
    private MetaHeader metaHeader;

    /** */
    private final List<MetaRecord> metaRecords = new ArrayList<>();

    /** */
    public SpecialHeader getSpecialHeader() {
        return specialHeader;
    }

    /** */
    public MetaHeader getMetaHeader() {
        return metaHeader;
    }

    /** */
    public List<MetaRecord> getMetaRecords() {
        return metaRecords;
    }

    /** render size */
    private Dimension size = new Dimension();

    /** */
    public static WindowsMetafile readFrom(InputStream is) throws IOException {
        WindowsMetafile metafile = new WindowsMetafile();

        metafile.specialHeader = SpecialHeader.readFrom(is);
        metafile.metaHeader = MetaHeader.readFrom(is);

        while (is.available() > 0) {
            MetaRecord metaRecord = MetaRecord.readFrom(is);
            metafile.metaRecords.add(metaRecord);
        }

        int width = Math.abs(metafile.specialHeader.right - metafile.specialHeader.left);
        int height = Math.abs(metafile.specialHeader.bottom - metafile.specialHeader.top);
        int inch = metafile.specialHeader.inch;

        float ratio = (float) inch / Toolkit.getDefaultToolkit().getScreenResolution();
        if (ratio < 1) {
            ratio = 1;
        }

        metafile.size.width = (int) (width / ratio);
        metafile.size.height = (int) (height / ratio);
Debug.println("inch: " + inch + ", ratio: " + ratio + ", " + Toolkit.getDefaultToolkit().getScreenResolution() + ", (" + metafile.size.width + ", " + metafile.size.height + "), (" + width + ", " + height + ")");

        return metafile;
    }

    /** */
    private static final int blueMask = 0xff0000;

    /** */
    private static final int greenMask = 0x00ff00;

    /** */
    private static final int redMask = 0x0000ff;

    /** */
    public static Color toColor(int bgr) {
        int blue  = (bgr & blueMask ) >> 16;
        int green = (bgr & greenMask) >> 8;
        int red   =  bgr & redMask;
        return new Color(red, green, blue);
    }

    /** Gets rendered size */
    public Dimension getSize() {
        return size;
    }

    /** Sets rendering size */
    public void setSize(Dimension size) {
        this.size = size;
    }

    /**
     * TODO each renderer switch clause -> Parser
     * TODO Parser#addRenderer
     * <pre>
     *  Parser#drawXXX ... ???
     * </pre>
     */
    interface Renderer<T> {
        /** TODO */
        void init(Dimension size);
        /** */
        void render(WmfContext context, MetaRecord metaRecord, boolean fromSelect, boolean play);
        /** TODO */
        void term();
        /** TODO */
        T getResult();
    }

    /** image renderer */
    private Renderer<?> renderer;

    /** */
    public void setRenderer(Renderer<?> renderer) {
        this.renderer = renderer;
    }

    /** */
    public Object render() {
        WmfContext context = new WmfContext(size, specialHeader.inch);
        renderer.init(size);
Debug.println("renderer: " + renderer.getClass().getName());
        for (MetaRecord metaRecord : metaRecords) {
            renderer.render(context, metaRecord, false, true);
            context.recordIndex++;
        }
        renderer.term();
        return renderer.getResult();
    }
}
