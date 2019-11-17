-- :name insert-server-table :! :n
-- :doc add a server to db
INSERT INTO server
(ip, country, country_code, region, region_name, city, isp, org, as_info, domain, longitude, latitude, tag)
VALUES (inet(:query), :country, :countryCode, :region, :regionName, :city, :isp, :org, :as, :domain, :lon, :lat, :tag)
ON CONFLICT (ip) DO NOTHING;

-- :name query-all-ip-from-server :? :*
SELECT ip FROM server;