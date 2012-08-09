USAPROXYREPORTGENERATOR - HTML report generator for UsaProxyFork logs
version 0.0.2-SNAPSHOT
Copyright (C) 2012 Teemu Pääkkönen - University of Tampere


DESCRIPTION
This software creates HTML reports from UsaProxyFork logs. Run it with:
`java -jar usaproxyreportgenerator.jar [options] <path-to-log.txt>`
You can specify multiple log files to have the generator create reports
from each one.

Options:
-outputDir <dir>		Specifies the output directory for HTML files. This
						directory must exist. In order to open the reports in
						a browser, you must also copy the `js` and `css`
						directories.
-dataProvider <class>  Specifies the full name of the data provider class.
                       Data providers instruct the generator on how to produce
                       plot data points from the log data. External data 
                       provider JARs should be put in the `plugins/` directory.
                       
                       
DATA PROVIDER PLUGINS
Data providers instruct the generator on how to produce plot data points from
log data. There is a default data provider that provides plot data for regular
UsaProxy log data. In addition, it is possible to have plugins that provide 
other ways to extract plot data from the parsed log files. The `dataProvider`
command line option can be used to change the data provider class. The JAR file
that contains the specified data provider must reside in the `plugins/`
directory.


WRITING A DATA PROVIDER PLUGIN CLASS
1. Have the class extend fi.uta.infim.usaproxyreportgenerator.DataProvider
2. Implement the methods defined in 
    fi.uta.infim.usaproxyreportgenerator.IBrowsingDataProvider - use the
    DefaultDataProvider class as an example
3. Compile and package your class in a JAR file
4. Put the JAR file in the `plugins/` directory
5. Use the `dataProvider` command line option when running the application


LICENSE
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
