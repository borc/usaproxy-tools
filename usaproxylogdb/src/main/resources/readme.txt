USAPROXYLOG2DB - tool for storing UsaProxy-fork logs into a database
version 0.0.2-SNAPSHOT
Copyright (C) 2012 Teemu Pääkkönen - University of Tampere


DESCRIPTION
This software reads UsaProxyFork logs and stores them in a database.


PRE-REQUISITES
You need a database driver and a dialect class for your database. Several
dialect classes are already provided with the distribution. See e.g.
 http://www.javabeat.net/qna/163-list-of-hibernate-sql-dialects/
for a list. A driver can be obtained from the database vendor.
Place the driver in the `lib` directory.

You also need to set up the hibernate.cfg.xml configuration file to suit
your database.


RUNNING
 
Run it with:
`java -jar usaproxylog2db.jar <path-to-log.txt>`

All http traffic log files must also be available at the log's location.
 

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
    
