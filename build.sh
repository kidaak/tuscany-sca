#!/bin/sh

#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#  
#    http://www.apache.org/licenses/LICENSE-2.0
#    
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.


if [ x$LIBXML2_INCLUDE = x ]; then
echo "LIBXML2_INCLUDE not set"
exit;
fi
if [ x$LIBXML2_LIB = x ]; then
echo "LIBXML2_LIB not set"
exit;
fi

if [ x$AXIS2C_HOME = x ]; then
echo "AXIS2C_HOME not set. not building SDO Axiom utility"
WITH_AXIS2C=--with-axis2c=false
else
echo "Using Axis2C installed at $AXIS2C_HOME"
WITH_AXIS2C=--with-axis2c=true
fi

TUSCANY_SDOCPP_HOME=`pwd`
cd ${TUSCANY_SDOCPP_HOME}/samples
./autogen.sh

cd $TUSCANY_SDOCPP_HOME
./autogen.sh

if [ x$TUSCANY_SDOCPP = x ]; then
TUSCANY_SDOCPP=`pwd`/deploy
fi

./configure --prefix=${TUSCANY_SDOCPP} ${WITH_AXIS2C} --enable-static=no
make
make install
