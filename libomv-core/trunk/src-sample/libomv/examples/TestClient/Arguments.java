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
