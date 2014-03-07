/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2014, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names
 *   of its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv.examples.TestClient;

import java.util.HashMap;
import java.util.regex.Pattern;

public class Arguments
{
    // Variables
    private HashMap<String, String> Parameters;

    // Constructor
    public Arguments(String[] Args)
    {
        Parameters = new HashMap<String, String>();
        
        Pattern Splitter = Pattern.compile("^-{1,2}|=", Pattern.CASE_INSENSITIVE);

        String Parameter = null;
        String[] Parts;

        // Valid parameters forms:
        // {-,/,--}param{ ,=,:}((",')value(",'))
        // Examples: 
        // -param1 value1 --param2
        //   /param4=happy -param5 '--=nice=--'
        for (String Txt : Args)
        {
            // Look for new parameters (-,/ or --) and a
            // possible enclosed value (=,:)
            Parts = Splitter.split(Txt, 3);

            switch (Parts.length)
            {
                // Found a value (for the last parameter 
                // found (space separator))
                case 1:
                    if (Parameter != null)
                    {
                        if (!Parameters.containsKey(Parameter))
                        {
                        	Parts[0] = Parts[0].replace("^['\"]?(.*?)['\"]?$", "$1");

                            Parameters.put(Parameter, Parts[0]);
                        }
                        Parameter = null;
                    }
                    // else Error: no parameter waiting for a value (skipped)
                    break;

                // Found just a parameter
                case 2:
                    // The last parameter is still waiting. 
                    // With no value, set it to true.
                    if (Parameter != null)
                    {
                        if (!Parameters.containsKey(Parameter))
                            Parameters.put(Parameter, "true");
                    }
                    Parameter = Parts[1];
                    break;

                // Parameter with enclosed value
                case 3:
                    // The last parameter is still waiting. 
                    // With no value, set it to true.
                    if (Parameter != null)
                    {
                        if (!Parameters.containsKey(Parameter))
                            Parameters.put(Parameter, "true");
                    }

                    Parameter = Parts[1];

                    // Remove possible enclosing characters (",')
                    if (!Parameters.containsKey(Parameter))
                    {
                    	Parts[2] = Parts[2].replace("^['\"]?(.*?)['\"]?$", "$1");
                        Parameters.put(Parameter, Parts[2]);
                    }

                    Parameter = null;
                    break;
            }
        }
        // In case a parameter is still waiting
        if (Parameter != null)
        {
            if (!Parameters.containsKey(Parameter))
                Parameters.put(Parameter, "true");
        }
    }

    // Retrieve a parameter value if it exists 
    public String get(String Param)
    {
        return Parameters.get(Param);
    }
}
