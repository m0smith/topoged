(ns topoged.file
  (:use [clojure.contrib.duck-streams :only (  *buffer-size* )])
  (:import
   (java.io BufferedReader BufferedInputStream BufferedOutputStream File FileInputStream FileOutputStream InputStream OutputStream)
   (java.net MalformedURLException Socket URL URI)))
  

(defmulti #^{:tag InputStream
             :doc "Attempts to coerce its argument into an open
java.io.BufferInputStream. Argument may be an instance of Reader,
BufferedReader, InputStream, File, URI, URL, Socket, or String.

If argument is a String, it tries to resolve it first as a URI, then
as a local file name. URIs with a 'file' protocol are converted to
local file names. Uses *default-encoding* as the text encoding.

Should be used inside with-open to ensure the OutputStream is properly
closed."
             :arglists '([x])}
  input-stream class)

(defmethod input-stream InputStream [#^InputStream x]
  (BufferedInputStream. x))

(defmethod input-stream File [#^File x]
  (input-stream (FileInputStream. x)))

(defmethod input-stream URL [#^URL x]
  (input-stream (if (= "file" (.getProtocol x))
            (FileInputStream. (.getPath x))
            (.openStream x))))

(defmethod input-stream URI [#^URI x]
  (input-stream (.toURL x)))

(defmethod input-stream String [#^String x]
  (try (let [url (URL. x)]
         (input-stream url))
       (catch MalformedURLException e
         (input-stream (File. x)))))

(defmethod input-stream Socket [#^Socket x]
  (input-stream (.getInputStream x)))

(defmethod input-stream :default [x]
	   (throw (Exception. (str "Cannot open " (pr-str x) " as an output stream."))))

(defmulti #^{:tag BufferedOutputStream
             :doc "Attempts to coerce its argument into an open java.io.BufferInputStream. Argument may be an
instance of  OutputStream, File, URI, URL, Socket, or String.

If argument is a String, it tries to resolve it first as a URI, then
as a local file name. URIs with a 'file' protocol are converted to
local file names.

Should be used inside with-open to ensure the OutputStream is properly
closed."
             :arglists '([x])}
  output-stream class)


(defmethod output-stream BufferedOutputStream [#^BufferedOutputStream x]
	   x)

(defmethod output-stream OutputStream [#^OutputStream x]
	   (BufferedOutputStream. x))

(defmethod output-stream File [#^File x]
	   (output-stream (FileOutputStream. x)))

(defmethod output-stream URL [#^URL x]
  (if (= "file" (.getProtocol x))
    (output-stream (File. (.getPath x)))
    (throw (Exception. (str "Cannot write to non-file URL <" x ">")))))

(defmethod output-stream URI [#^URI x]
  (output-stream (.toURL x)))

(defmethod output-stream String [#^String x]
  (try (let [url (URL. x)]
         (output-stream url))
       (catch MalformedURLException err
         (output-stream (File. x)))))

(defmethod output-stream Socket [#^Socket x]
  (output-stream (.getOutputStream x)))

(defmethod output-stream :default [x]
  (throw (Exception. (str "Cannot open <" (pr-str x) "> as an output-stream."))))


(defn copy-md5
  "Copy input to output and return the MD5 hash of the stream.
Based in duck-streams copy"
  [#^InputStream input #^OutputStream output]
  
  (let [buffer (make-array Byte/TYPE *buffer-size*)
	digest (java.security.MessageDigest/getInstance "MD5")]
    (loop []
      (let [size (.read input buffer)]
        (if (pos? size)
          (do (.write output buffer 0 size)
	      (.update digest buffer 0 size)
              (recur))
	  (.toString (java.math.BigInteger. 1 (.digest digest)) 16)))))) 