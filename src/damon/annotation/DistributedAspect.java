/*****************************************************************************************
 * Damon : a Distributed AOP Middleware on top of a p2p Overlay Network
 * Copyright (C) 2006-2008 Ruben Mondejar
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *****************************************************************************************/
package damon.annotation;

import java.lang.annotation.*;

import damon.util.Context;


/**
 * This annotation refers to the definition of pointcut abstractions in Damon
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DistributedAspect {
  Abstractions abstraction()   default Abstractions.LOCAL;
  String target()              default Context.URL_GLOBAL;
  int num()			           default 1; //many pointcut condition
  boolean proxy()              default false;  
  boolean singleton()          default false;
  boolean stateful()           default false;
  
}
