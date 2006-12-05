/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/* $Rev$ $Date$ */

#include "commonj/sdo/SDOUtils.h"
#include <stdio.h>
using namespace std;

//
// Utility methods to print a DataObject tree
//

namespace commonj {
    namespace sdo {
        
        const char *SDOUtils::CDataStartMarker    = "XXXCDATA@STARTXXX";
        const char *SDOUtils::XMLCDataStartMarker = "<![CDATA[";
        const char *SDOUtils::CDataEndMarker      = "XXXCDATA@ENDX";
        const char *SDOUtils::XMLCDataEndMarker   = "]]>";        

//////////////////////////////////////////////////////////////////////////
// Conversions
//////////////////////////////////////////////////////////////////////////


        std::map<std::string,std::string> SDOUtils::SdoToXsd;

        std::map<std::string,std::string> SDOUtils::XsdToSdo;

        bool SDOUtils::populated = false;

        bool SDOUtils::populate()
        {
            SDOUtils::XsdToSdo["ID"]     = "String";
            SDOUtils::XsdToSdo["NCName"] = "String";
            SDOUtils::XsdToSdo["string"] = "String";
            SDOUtils::XsdToSdo["anyType"] = "DataObject";
            SDOUtils::XsdToSdo["int"] = "Integer";
            SDOUtils::XsdToSdo["integer"] = "Integer";
            SDOUtils::XsdToSdo["negativeInteger"] = "Integer";
            SDOUtils::XsdToSdo["nonNegativeInteger"] = "Integer";
            SDOUtils::XsdToSdo["positiveInteger"] = "Integer";
            SDOUtils::XsdToSdo["nonpositiveInteger"] = "Integer";
            SDOUtils::XsdToSdo["unsignedShort"] = "Integer";
            SDOUtils::XsdToSdo["unsignedInt"] = "Integer";
            SDOUtils::XsdToSdo["unsignedLong"] = "Long";
            SDOUtils::XsdToSdo["double"] = "Double";
            SDOUtils::XsdToSdo["short"] = "Short";
            SDOUtils::XsdToSdo["unsignedByte"] = "Short";
            SDOUtils::XsdToSdo["float"] = "Float";
            SDOUtils::XsdToSdo["boolean"] = "Boolean";
            SDOUtils::XsdToSdo["byte"] = "Byte";
            SDOUtils::XsdToSdo["base64Binary"] = "Bytes";
            SDOUtils::XsdToSdo["hexBinary"] = "Bytes";
            SDOUtils::XsdToSdo["anyURI"] = "URI";
            SDOUtils::XsdToSdo["QName"] = "URI";

            SDOUtils::SdoToXsd["String"]     = "string";
            SDOUtils::SdoToXsd["DataObject"] = "anyType";
            SDOUtils::SdoToXsd["Integer"] = "integer";
            SDOUtils::SdoToXsd["Long"] = "unsignedLong";
            SDOUtils::SdoToXsd["Double"] = "double";
            SDOUtils::SdoToXsd["Short"] = "short";
            SDOUtils::SdoToXsd["Float"] = "float";
            SDOUtils::SdoToXsd["Boolean"] = "boolean";
            SDOUtils::SdoToXsd["Byte"] = "byte";
            SDOUtils::SdoToXsd["Bytes"] = "base64Binary";
            SDOUtils::SdoToXsd["URI"] = "anyURI";

            SDOUtils::populated = true;
            return true;
        }

        const char* SDOUtils::SDOToXSD(const char* sdoname)
        {
            if (sdoname == 0)
            {
                return SDOUtils::SdoToXsd["String"].c_str();
            }
            if (!SDOUtils::populated) SDOUtils::populate();
            string& s =  SDOUtils::SdoToXsd[sdoname];
            if (s.empty())
            {
                return SDOUtils::SdoToXsd["String"].c_str();
            }
            return s.c_str();
        }


        const char* SDOUtils::XSDToSDO(const char* xsdname)
        {
            if (xsdname == 0)
            {
                return SDOUtils::XsdToSdo["string"].c_str();
            }
            if (!SDOUtils::populated) SDOUtils::populate();
            string& s = SDOUtils::XsdToSdo[xsdname];
            if (s.empty())
                return SDOUtils::XsdToSdo["string"].c_str();
            return s.c_str();
        }

//////////////////////////////////////////////////////////////////////////
// Print Tabs
//////////////////////////////////////////////////////////////////////////

        void SDOUtils::printTabs(ostream& out, unsigned int incr)
        {
            for (unsigned int ind=0; ind < incr; ind++)
            {
                out << "  ";
            }
        }

//////////////////////////////////////////////////////////////////////////
// Print a DatObject tree
//////////////////////////////////////////////////////////////////////////

        void SDOUtils::printDataObject(ostream& out, DataObjectPtr dataObject)
        {
            printDataObject(out, dataObject,0);
        }


        void SDOUtils::printDataObject(ostream& out ,DataObjectPtr dataObject,
            unsigned int incr)
        {
    
            out << ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start of DO" 
                 << endl;
    
            if (!dataObject)return;

            const Type& dataObjectType = dataObject->getType();
            printTabs(out, incr);
            out << "DataObject type: " 
                 << dataObjectType.getURI() 
                 << "#" << dataObjectType.getName() << endl;
    
            incr++;
    
            //////////////////////////////////////////////////////////////
            // Iterate over all the properties
            //////////////////////////////////////////////////////////////
            PropertyList pl = dataObject->getInstanceProperties();
            for (unsigned int i = 0; i < pl.size(); i++)
            {
                printTabs(out, incr);
                out << "Property: " << pl[i].getName() << endl;
        
                const Type& propertyType = pl[i].getType();
        
                printTabs(out, incr);

                out << "Property Type: " 
                     << propertyType.getURI() 
                     << "#" << propertyType.getName() << endl;
        
                if (dataObject->isSet(pl[i]))
                {
            
                    ///////////////////////////////////////////////////////////
                    // For a many-valued property get the list of values
                    ///////////////////////////////////////////////////////////
                    if (pl[i].isMany())
                    {
                        incr++;
                        DataObjectList& dol = dataObject->getList(pl[i]);
                        char cc[20];
                        for (unsigned int j = 0; j <dol.size(); j++)
                        {
                            printTabs(out, incr);
                            // seems to be a bug in ostream? Will not print j 
                            // as an integer.
                            out << "Value ";
                            sprintf(cc,"%d",j);
                            out << cc;
                            out << endl;
                            incr++;
                            printDataObject(out, dol[j],incr);
                            incr--;
                            out << endl;
                        }
                        incr--;
                    } // end IsMany
        
                    //////////////////////////////////////////////////////////////////////
                    // For a primitive data type print the value
                    //////////////////////////////////////////////////////////////////////
                    else if (propertyType.isDataType())
                    {
                        printTabs(out, incr);
                        out<< "Property Value: " 
                            << dataObject->getCString(pl[i]) <<endl ; 
                    }
            
                    //////////////////////////////////////////////////////////////////////
                    // For a dataobject print the do
                    //////////////////////////////////////////////////////////////////////
                    else
                    {
                        incr++;
                        printDataObject(out, dataObject->getDataObject(pl[i]),incr);
                        incr--;
                    }
                }
                else
                {
                    printTabs(out, incr);
                    out << "Property Value: not set" <<endl ; 
                }
        
            }
            incr--;
            out << "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< end of do" << endl;
        }

        
        void SDOUtils::printTypes(std::ostream& out, DataFactoryPtr df) 
        {
            TypeList tl = df->getTypes();
            for (unsigned int i = 0; i < tl.size(); i++)
            {
                out << "Type: " << tl[i].getURI()<< "#" << tl[i].getName() <<
                    " isOpen: " << tl[i].isOpenType()
                    << " isSequenced: " << tl[i].isSequencedType() << endl;
                PropertyList pl = tl[i].getProperties();
                for (unsigned int j = 0; j < pl.size(); j++)
                {
                    out << "\tProperty: " << pl[j].getName()
                        << " type: " <<pl[j].getType().getURI()<<"#"<<pl[j].getType().getName()<<
                        " isMany: " << pl[j].isMany() << endl;
                    
                }
            }
            
        }
        
        /*
         * A local utility function that replaces one string with and another within a
         * host string and adjusts the lenght of the host string accordingly.
         */ 
        SDOString SDOUtils::replace(SDOString hostString, const char *fromString, const char *toString)
        {
            SDOString returnString("");

            // find and replace all occurances of fromString with toString. The start, end
            // and length variables are used to indicate the start, end and length
            // of the text sections to be copied from the host string to the return
            // string. toString is appended in between these copied sections because the
            // string is broken whenever fromString is found
            std::string::size_type start  = 0;
            std::string::size_type end    = hostString.find(fromString, 0);
            std::string::size_type length = 0;

            while ( end != std::string::npos )
            {
                // copy all the text up to the fromString
                length = end - start;
                returnString.append(hostString.substr(start, length));

                // add in the toString
                returnString.append(toString);

                // find the next fromString
                start = end + strlen(fromString);
                end = hostString.find(fromString, start);
            }

            // copy any text left at the end of the host string
            returnString.append(hostString.substr(start));

            return returnString;
        }
        

    };
};
