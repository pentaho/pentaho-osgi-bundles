/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright 2002 - 2017 Pentaho Corporation. All rights reserved.
 */

require.config({
 "paths": {
   "common-ui/angular": "/webjars/angular",
   "common-ui/angular-route": "/webjars/angular-route",
   "common-ui/angular-resource": "/webjars/angular-resource",
   "angular-mocks": "/webjars/angular-mocks"
 },
 "shim" : {
   "common-ui/angular" : { "exports": "angular" },
   "common-ui/angular-route" : { "deps": ["common-ui/angular"] },
   "common-ui/angular-resource" : { "deps": ["common-ui/angular"] },
   "angular-mocks" : { "deps": ["common-ui/angular"] }
 }
});
