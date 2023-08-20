# deploy:

subproject for automating spinning-up a new solr cluster for ease of local development

## cluster setup:
1. invoke `./gradlew deploy:build`
2. cd deploy/
3. start: `docker-compose up -d`
4. stop: `docker-compose down`

other alternatives:
starting solr natively (ref: https://solr.apache.org/guide/solr/latest/deployment-guide/installing-solr.html)
