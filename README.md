[![Release](https://jitpack.io/v/umjammer/vavi-image-anigif.svg)](https://jitpack.io/#umjammer/vavi-image-anigif)
[![Java CI](https://github.com/umjammer/vavi-image-anigif/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/vavi-image-anigif/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/vavi-image-anigif/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/vavi-image-anigif/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)
[![Parent](https://img.shields.io/badge/Parent-vavi--image--sandbox-pink)](https://github.com/umjammer/vavi-image-sandbox)

# vavi-image-anigif

🎨 Imaging the world more and more!

## ImageIO

  * gif animation
  * susie (windows only)
  * svg

## filter

  * blob detection
  * ~~face detection~~ obsolete, use rococoa vision 

## References

 * wmf
   * https://github.com/camullen/MetafileReader (java)
   * https://github.com/kareldonk/WMFPreview (c++)
 * svg
   * https://developer.mozilla.org/ja/docs/Web/SVG/Attribute/d
   * https://github.com/blackears/svgSalamander/tree/master/svg-core/src/main/java/com/kitfox/svg/pathcmd

## TODO

 * ~~Gif Animation imageio~~
 * enhanced g2d
   * https://github.com/eseifert/vectorgraphics2d
   * https://github.com/freehep/freehep-vectorgraphics
 * svg
   * ~~SVG StAX~~ use serdes
   * ~~SvgRenderer use JAXP~~
 * wmf
   * WMF imageio
   * https://github.com/apache/poi
 * postscript
   * https://github.com/jimirich/ghost4j
   * https://xmlgraphics.apache.org/commons/postscript.html (g2d)
   * https://github.com/mhschmieder/epstoolkit