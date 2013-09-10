/*
 * This file is distributed as part of the SkySQL Cloud Data Suite.  It is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * version 2.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Copyright 2013 SkySQL Ab
 */

package com.skysql.monitor;

import java.text.DecimalFormat;

/**
 * The Global Monitor class.
 * 
 * This is used to efficiently collect monitor data that is based on the global 
 * variables and global status data available within the information_schema of
 * the MySQL database. The data is collected once per probe cycle from each database
 * in the cluster and reused by multiple instances of the globalMonitor class.
 * 
 * @author Mark Riddoch
 *
 */
public class globalMonitor extends monitor {
	
	/**
	 * The singleton class associated with this node that manages the
	 * collection and storage of global variables and global status
	 * data from the database server being monitored.
	 */
	private	globalStatusObject	m_global;
	/**
	 * The monitor should record differences between the consecutive
	 * probe cycles.
	 */
	private boolean				m_delta;
	/**
	 * The last value probed if this is a delta monitor
	 */
	private	Long				m_lastAbsValue = null;
	
	
	/**
	 * Constructor for the global monitor
	 * 
	 * @param db		Handle for the monitoring database
	 * @param id		The ID of the monitor
	 * @param mon_node	The node being monitored
	 * @param delta		The monitor is a delta monitor
	 */
	public globalMonitor(mondata db, int id, node mon_node, boolean delta)
	{
		super(db, id, mon_node);
		m_global = globalStatusObject.getInstance(mon_node);
		m_delta = delta;
	}
	
	/**
	 * Probe the global data. Uses this the instance of the global status and
	 * variables manager within the monitor to access the latest collected
	 * data and return the value for the monitor.
	 * 
	 * @param	verbose	Control verbose logging of the collected data
	 */
	public void probe(boolean verbose)
	{
		String value = m_global.getStatusOrVaraiable(m_sql);
		if (verbose)
			System.out.println("globalMonitor: " + m_sql + " set value " + value);
		if (m_delta)
		{
			if (m_lastAbsValue != null)
			{
				Long	absValue = new Long(value);
				Long delta = absValue - m_lastAbsValue;
				if (delta < 0)
				{
					System.out.println("Negative delta value for probe, absolute value is " + absValue + " last absolute value " + m_lastAbsValue);
					delta = new Long(0);
				}
				DecimalFormat format = new DecimalFormat("###############0");
				String deltaStr = format.format(delta.longValue());
				saveObservation(deltaStr);
				m_lastValue = deltaStr;
				m_lastAbsValue = absValue;
			}
			else
			{
				m_lastAbsValue = new Long(value);
			}
		}
		else
		{
			saveObservation(value);
		}
		m_lastValue = value;
	}

}