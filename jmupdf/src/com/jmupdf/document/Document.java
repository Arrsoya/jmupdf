/*
 * 
 * See copyright file
 *  
 */
package com.jmupdf.document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.jmupdf.JmuPdf;
import com.jmupdf.exceptions.DocException;
import com.jmupdf.interfaces.DocumentTypes;
import com.jmupdf.interfaces.ImageTypes;
import com.jmupdf.interfaces.TifTypes;
import com.jmupdf.page.Page;
import com.jmupdf.page.PageLinks;
import com.jmupdf.page.PageText;

/**
 * Document class
 * 
 * @author Pedro J Rivera
 *
 */
public abstract class Document extends JmuPdf implements DocumentTypes, ImageTypes, TifTypes {
	private String document;
	private String password;
	private long handle;
	private int pageCount;		
	private int type;
	private int antiAliasLevel;
	private int maxStore;
	private boolean isCached;		
	private Outline outline;

	/**
	 * Open a document
	 * @param document
	 * @param password
	 * @param type
	 * @param maxStore
	 * @throws DocException
	 */
	public void open(String document, String password, int type, int maxStore) throws DocException {
		this.document = document;
		this.password = password;
		this.type = type;
		this.maxStore = maxStore << 20;
		this.handle = 0;
		this.pageCount = 0;
		this.isCached = false;

		File file = new File(document);

		if (!file.exists()) {
			throw new DocException("Document " + document + " does not exist.");
		}

		if (type == DOC_PDF) {
			this.handle = pdfOpen(document, password, getMaxStore());
		} else if (type == DOC_XPS) {
			this.handle = xpsOpen(document, getMaxStore());
		}

		if (this.handle > 0) {
			this.pageCount = getPageCount(this.handle);
			this.antiAliasLevel = getAntiAliasLevel(this.handle);
		}
	}

	/**
	 * Open a document
	 * @param document
	 * @param password
	 * @param type
	 * @param maxStore
	 * @throws DocException
	 */
	public void open(byte[] document, String password, int type, int maxStore) throws DocException {
		try {
			File tmp = File.createTempFile("jmupdf" + getClass().hashCode(), ".tmp");
			tmp.deleteOnExit();

			FileOutputStream fos = new FileOutputStream(tmp.getAbsolutePath(), true);
            fos.write(document, 0, document.length);
            fos.flush();
            fos.close();

            open(tmp.getAbsolutePath(), password, type, maxStore);
    		isCached = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close document
	 */
	public void close() {
		if (handle > 0) {
			if (getType() == DOC_PDF) {
				pdfClose(handle);
			} else if (getType() == DOC_XPS) {
				xpsClose(handle);
			}
			if (isCached) {
				File file = new File(document);
				if (file.exists()) {
					file.delete();
				}
			}
			if (outline != null) {
				disposeOutline(outline);
			}
			handle = 0;
		}
	}

	/**
	 * Get document handle
	 * @return
	 */
	public long getHandle() {
		return handle;
	}

	/**
	 * Get document type
	 * @return
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Get max memory used to store pdf information.
	 * @return
	 */
	public int getMaxStore() {
		if (maxStore <= 0) {
			maxStore = 60 << 20;
		}
		return maxStore;
	}
	
	/**
	 * Get document version
	 * @return
	 */
	public int getVersion() {
		if (handle > 0) {
			if (getType() == DOC_PDF) {
				return pdfVersion(handle);
			} else {
				//TODO: Get xps version
			}
		}
		return 0;
	}

	/**
	 * Get total pages in document
	 * @return 
	 */
	public int getPageCount() {
		if (handle > 0) {
			return pageCount;
		}
		return 0;
	}

	/**
	 * Get document full path plus name
	 * @return
	 */
	public String getDocumentName() {
		if (handle > 0) {
			return document;
		}
		return null;
	}
	
	/**
	 * Get document password
	 * @return
	 */
	public String getPassWord() {
		return password;
	}
	
	/**
	 * Get a page object.  
	 * @param page
	 * @return
	 */
	public Page getPage(int page) {
		if (handle > 0) {
			return new Page(this, getPageInfo(page), page);
		}
		return null;
	}

	/**
	 * Get page info
	 * @param page
	 * @return
	 */
	public float[] getPageInfo(int page) {
		if (handle > 0) {
			return loadPage(handle, page);
		}
		return null;
	}

	/**
	 * Get page links
	 * @param page
	 * @return
	 */
	public PageLinks[] getPageLinks(int page) {
		if (handle > 0) {
			if (getType() == DOC_PDF) {
				return pdfGetPageLinks(handle, page);
			} else if (getType() == DOC_XPS) {
				// TODO: Get xps links
			}
		}
		return null;
	}
	
	/**
	 * Get document outline
	 * @return
	 */
	public Outline getOutline() {
		if (handle > 0) {
			if (outline == null) {
				outline = getOutline(handle);	
			}
			return outline;
		}
		return null;
	}
	
	/**
	 * Get page text
	 * @param page
	 * @param zoom
	 * @param rotate
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return
	 */
	public PageText[] getPageText(int page, float zoom, int rotate, int x0, int y0, int x1, int y1) {
		if (handle > 0) {
			return getPageText(handle, page, zoom, rotate, x0, y0, x1, y1);
		}
		return null;
	}

	/**
	 * Get pixels for a page
	 * @param page
	 * @param zoom
	 * @param rotate
	 * @param color
	 * @param bbox
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return
	 */
	public Object getPagePixels(int page, float zoom, int rotate, int color, int[] bbox, float x0, float y0, float x1, float y1) {
		if (handle > 0) {
			return getPixMap(handle, page, zoom, rotate, color, bbox, x0, y0, x1, y1);
		}
		return null;
	}

	/**
	 * Set default anti-alias level to be used when rendering pages
	 * @param antiAliasLevel
	 */
	public void setAntiAliasLevel(int antiAliasLevel) {
		if (handle > 0) {
			antiAliasLevel = validateAntiAliasLevel(antiAliasLevel);
			if (antiAliasLevel != this.antiAliasLevel) {
				this.antiAliasLevel = antiAliasLevel;
				setAntiAliasLevel(handle, antiAliasLevel);
			}
		}
	}

	/**
	 * Get default anti-alias level used when rendering pages
	 * @return
	 */
	public int getAntiAliasLevel() {
		if (handle > 0) {
			return antiAliasLevel;
		}
		return -1;
	}
	
	/**
	 * Save a page to a file in PBM format</br>
	 * PBM is the portable bitmap format, a lowest common denominator monochrome file format. 
	 * @param page
	 * @param file
	 * @param zoom
	 * @return
	 */
	public boolean saveAsPbm(int page, String file, float zoom) {
		if (handle > 0) {
			return writePbm(handle, page, zoom, file) == 0;
		}
		return false;
	}

	/**
	 * Save a page to a file in PNM format
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @return
	 */
	public boolean saveAsPnm(int page, String file, float zoom, int color) {
		if (handle > 0) {
			if (color == IMAGE_TYPE_RGB || 
				color == IMAGE_TYPE_GRAY) {
				return writePnm(handle, page, zoom, color, file) == 0;
			}
		}
		return false;
	}
	

	/**
	 * Save a page to a file in JPEG format
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @param quality
	 * <blockquote>quality levels are in the range 0-100 with a default value of 75.</blockquote>
	 * @return
	 */
	public boolean saveAsJPeg(int page, String file, float zoom, int color, int quality) {
		if (handle > 0) {
			if (color == IMAGE_TYPE_RGB || 
				color == IMAGE_TYPE_GRAY) {
				if (!(quality >= 0 && quality <= 100)) {
					quality = 75;
				}
				return writeJPeg(handle, page, zoom, color, file, quality) == 0;
			}
		}
		return false;
	}

	/**
	 * Save a page to a BMP format
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @return
	 */
	public boolean saveAsBmp(int page, String file, float zoom, int color) {
		if (handle > 0) {
			if (color == IMAGE_TYPE_RGB    || 
				color == IMAGE_TYPE_GRAY   ||
				color == IMAGE_TYPE_BINARY ||
				color == IMAGE_TYPE_BINARY_DITHER) {
				return writeBmp(handle, page, zoom, color, file) == 0;
			}
		}
		return false;
	}
	
	/**
	 * Save a page to a file in PNG format
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @return
	 */
	public boolean saveAsPng(int page, String file, float zoom, int color) {
		if (handle > 0) {
			if (color == IMAGE_TYPE_RGB  || 
				color == IMAGE_TYPE_ARGB || 
				color == IMAGE_TYPE_GRAY) {
				return writePng(handle, page, zoom, color, file) == 0;
			}
		}
		return false;
	}

	/**
	 * Save a page to a file in PAM format</br>
	 * The name "PAM" is an acronym derived from "Portable Arbitrary Map"
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @return
	 */
	public boolean saveAsPam(int page, String file, float zoom, int color) {
		if (handle > 0) {
			if (color == IMAGE_TYPE_RGB  || 
				color == IMAGE_TYPE_ARGB || 
				color == IMAGE_TYPE_GRAY) {
				return writePam(handle, page, zoom, color, file) == 0;
			}
		}
		return false;
	}
	
	/**
	 * Save a page to a TIF format
	 * @param page
	 * @param file
	 * @param zoom
	 * @param color
	 * @param compression
	 * @param mode
	 * @param quality <br/><br/>
	 *        
	 *        <blockquote>
	 *        <strong>When compression == TIF_COMPRESSION_ZLIB <br/></strong> 
	 *          Control  the  compression  technique used by the Deflate codec.  <br/> 
	 *          Quality levels are in the range 1-9 with larger numbers yielding <br/> 
	 *          better compression at the cost of more computation. The default  <br/> 
	 *          quality level is 6 which yields a good time-space tradeoff.      <br/><br/>
	 *          
	 *        <strong>When compression == TIF_COMPRESSION_JPEG <br/></strong>
	 *          Control the compression quality level used in the baseline algo- <br/>
	 *          rithm. Note that quality levels are in the range 0-100 with a    <br/>
	 *          default value of 75. <br/><br/>
	 *        </blockquote>  
	 * @return
	 */
	public boolean saveAsTif(int page, String file, float zoom, int color, int compression, int mode, int quality) {
		if (handle > 0) {
			
			if (!(color == IMAGE_TYPE_RGB    || 
				  color == IMAGE_TYPE_ARGB   || 
				  color == IMAGE_TYPE_GRAY   ||
				  color == IMAGE_TYPE_BINARY || 
				  color == IMAGE_TYPE_BINARY_DITHER)) {
				log("Invalid color type specified.");
				return false;
			}
			
			if (!(mode == TIF_DATA_APPEND || 
				  mode == TIF_DATA_DISCARD)) {
				log("Invalid mode value specified.");
				return false;
			}

			if (compression == TIF_COMPRESSION_CCITT_RLE || 
				compression == TIF_COMPRESSION_CCITT_T_4 ||
				compression == TIF_COMPRESSION_CCITT_T_6) {
				if (!(color == IMAGE_TYPE_BINARY || 
					  color == IMAGE_TYPE_BINARY_DITHER)) {
					log("When using CCITT compression, color must be type binary");
					return false;
				}
				if (color == IMAGE_TYPE_ARGB) {
					log("When using CCITT compression, color cannot be IMAGE_TYPE_ARGB");
					return false;
				}
			}

			if (compression == TIF_COMPRESSION_JPEG) {
				if (!(quality >= 1 && quality <= 100)) {
					quality = 75;
				}
			}

			if (compression == TIF_COMPRESSION_ZLIB) {
				if (!(quality >= 1 && quality <= 9)) {
					quality = 6;
				}
			}

			return writeTif(handle, page, zoom, color, file, compression, mode, quality) == 0;
		}

		return false;
	}

	/**
	 * Validate Anti-alias level
	 */
	private static int validateAntiAliasLevel(int level) {
		if (level < 0) {
			return 0;
		}
		else if (level > 8) {
			return 8;
		}
		return level;
	}

	/**
	 * Release all references to outline objects
	 * @param o
	 * 
	 */
	private static void disposeOutline(Outline o) {
		while (o != null) {
			if (o.getChild() != null) {
				disposeOutline(o.getChild());
			}
			if (o.getNext() != null) {
				disposeOutline(o.getNext());
			}
			o = null;
		}
	}

}