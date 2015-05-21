## Updates for 02-23-2012 (V.0.4.1) ##

  1. Exposed more link types to outline class.
  1. Added new setAutoRotate() to PrintServices class.
  1. Fixed bug in swing viewer sample app where pages where not displayed in Linux with different regional settings other than us\_EN.
  1. Several other bug fixes.
  1. Many thanks to users who helped with testing and suggested improvements.


## Updates for 02-19-2012 (V.0.4.0) ##

  1. Exposed gamma feature for color correction.
  1. Added new PagePixel class to manage pixel data.
  1. Added new document type. Comic Book files (CBZ) can now be rendered.
  1. JNI and Java Code have been greatly simplified and optimized.
  1. Better handling of unicode characters when opening and exporting documents.
  1. Rendered pages can now be saved with "umlauts".
  1. Added support for XPS page links.
  1. Expanded link types returned in PageLinks class.
  1. Fixed issue when rendering grey images.
  1. More options available for generating buffered images. Now you have ARGB, ARGB\_PRE, RGB, BGR, BYTE\_BINARY, and BYTE\_GRAY.
  1. Updated examples to reflect changes.
  1. Updated test swing viewer to show off features. Make sure to right click and drag your mouse to see cropping and text selection in action.
  1. Removed array pinning from pixel creation in JNI code. This should help the GC as it won't be disabled during large batch rendering jobs.


## Updates for 01-24-2012 (V.0.3.0) ##

  1. Fixed a bug where margins were incorrect when rendering certain pdf's.
  1. The mupdf core now supports multi-threading and process locking.
    * All dependencies to PThreads are now removed.
  1. XPS documents can now generate outlines.
  1. Improved memory management. Now you can specify memory limits on a per document basis.
  1. Added better RGB vs ARGB support.
  1. Many minor bug fixes.