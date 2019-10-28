(ns rainmote.tools.apnic)

;;;;
;;;; 从APNIC获取分配的IP网段
;;;; wiki: https://zh.wikipedia.org/zh-hans/%E4%BA%9A%E5%A4%AA%E4%BA%92%E8%81%94%E7%BD%91%E7%BB%9C%E4%BF%A1%E6%81%AF%E4%B8%AD%E5%BF%83
;;;;
;;;; File names:
;;;;    http://ftp.apnic.net/apnic/stats/apnic/
;;;;    http://ftp.apnic.net/apnic/stats/apnic/delegated-apnic-latest
;;;;
;;;; File Header:
;;;;  2       | apnic    | 20190913 | 62218   | 19830613  | 20190912 | +1000
;;;;  version | registry | serial   | records | startdate | enddate  | UTCoffset
;;;;
;;;; [copy from APNIC readme]
;;;;
;;;; 3.2.3 	Records
;;;; ---------------
;;;;
;;;; After the defined file header, and excluding any space or comments,
;;;; each line in the file represents a single allocation (or assignment)
;;;; of a specific range of Internet number resources (IPv4, IPv6 or
;;;; ASN), made by the RIR identified in the record.
;;;;
;;;; IPv4  records may represent non-CIDR ranges or CIDR blocks, and
;;;; therefore the record format represents the beginning of range, and a
;;;; count. This can be converted to prefix/length using simple algorithms.
;;;;
;;;; IPv6 records represent the prefix and the count of /128 instances
;;;; under that prefix.
;;;;
;;;; Format:
;;;;
;;;; 	registry|cc|type|start|value|date|status[|extensions...]
;;;;
;;;; Where:
;;;;
;;;; 	registry  	The registry from which the data is taken.
;;;; 			For APNIC resources, this will be:
;;;;
;;;; 			    apnic
;;;;
;;;; 	cc        	ISO 3166 2-letter code of the organisation to
;;;; 	                which the allocation or assignment was made.
;;;;
;;;; 	type      	Type of Internet number resource represented
;;;; 			in this record. One value from the set of
;;;; 			defined strings:
;;;;
;;;; 			    {asn,ipv4,ipv6}
;;;;
;;;; 	start     	In the case of records of type 'ipv4' or
;;;; 			'ipv6' this is the IPv4 or IPv6 'first
;;;; 			address' of the	range.
;;;;
;;;; 			In the case of an 16 bit AS number, the
;;;; 			format is the integer value in the range:
;;;;
;;;; 			    0 - 65535
;;;;
;;;; 			In the case of a 32 bit ASN,  the value is
;;;; 			in the range:
;;;;
;;;; 			    0 - 4294967296
;;;;
;;;; 		    	No distinction is drawn between 16 and 32
;;;; 		    	bit ASN values in the range 0 to 65535.
;;;;
;;;; 	value     	In the case of IPv4 address the count of
;;;; 			hosts for this range. This count does not
;;;; 			have to represent a CIDR range.
;;;;
;;;; 			In the case of an IPv6 address the value
;;;; 			will be the CIDR prefix length from the
;;;; 			'first address'	value of <start>.
;;;;
;;;; 			In the case of records of type 'asn' the
;;;; 			number is the count of AS from this start
;;;; 			value.
;;;;
;;;; 	date      	Date on this allocation/assignment was made
;;;; 			by the RIR in the format:
;;;;
;;;; 			    YYYYMMDD
;;;;
;;;; 			Where the allocation or assignment has been
;;;; 			transferred from another registry, this date
;;;; 			represents the date of first assignment or
;;;; 			allocation as received in from the original
;;;; 			RIR.
;;;;
;;;; 			It is noted that where records do not show a
;;;; 			date of	first assignment, this can take the
;;;; 			0000/00/00 value.
;;;;
;;;;     	status    	Type of allocation from the set:
;;;;
;;;;                             {allocated, assigned}
;;;;
;;;;                 	This is the allocation or assignment made by
;;;;                 	the registry producing the file and not any
;;;;                 	sub-assignment by other agencies.
;;;;
;;;;    	extensions 	In future, this may include extra data that
;;;;    			is yet to be defined.


(def apnic-latest "http://ftp.apnic.net/apnic/stats/apnic/delegated-apnic-latest")

(defn get-content [country type]
  (->> (slurp apnic-latest)
       clojure.string/split-lines
       (filter #(clojure.string/includes? % (format "%s|%s" country type)) ,,,)
       (take 10)))

(defn get-cn-ip []
  (get-content "CN" "ipv4"))