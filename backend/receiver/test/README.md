1. Load database with testing-schema.sql, which will result in

	- 4 categories: NMQ, NMF, NWQ, NWF
	- 7 participants in each category
	- xx001 - xx007 for Qualifiying Rounds
	- xx501 - xx507 for Final Rounds
	- Qualifying Rounds are loaded with scores for Route 1

3. Testing is done on frisby.js (http://frisbyjs.com)

	- Install jasmine-node [npm install -g jasmine-node]

3. Edit the whatever file to point the address to your server

	- default: http://localhost:3000/

1. Start 'CRIMP.js' locally, or on Heroku

3. Run the test-suite [jasmine-node CRIMP-testing.js]
