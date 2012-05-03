USAPROXYLOGPARSER - Java API for UsaProxy-fork logs
version 0.0.2-SNAPSHOT
Copyright (C) 2012 Teemu Pääkkönen - University of Tampere


DESCRIPTION
This Java library provides an object based API for handling UsaProxy-fork
log files. It is not intended to be used with the original UsaProxy software.
See apidocs/index.html for API documentation.

The package also contains a JPA compliant orm.xml for O/R mapping. It is only
intended for very basic mapping; for anything more advanced you should write
your own mappings. The mapping file resides in the META-INF directory inside 
the JAR itself; the easiest way to access it is by using the classloader, e.g. 
 UsaProxyLog.class.getResource( "/META-INF/orm.xml" )


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
    