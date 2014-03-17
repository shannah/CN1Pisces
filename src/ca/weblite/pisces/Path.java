/*
 * Pisces User
 * Copyright (C) 2009 John Pritchard
 * Codename One Modifications Copyright (C) 2013 Steve Hannah
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.  The copyright
 * holders designate particular file as subject to the "Classpath"
 * exception as provided in the LICENSE file that accompanied this
 * code.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package ca.weblite.pisces;

/**
 * @see ca.weblite.pisces.d.LineSink
 * @see ca.weblite.pisces.d.PathSink
 * @see Graphics
 */
public class Path
    extends ca.weblite.pisces.d.PathStore
{

    
    public final int windingRule;
    

    public Path(){
        this(WIND_NON_ZERO);
    }
    public Path(int windingRule){
        super();
        this.windingRule = windingRule;
    }
    
    
}
