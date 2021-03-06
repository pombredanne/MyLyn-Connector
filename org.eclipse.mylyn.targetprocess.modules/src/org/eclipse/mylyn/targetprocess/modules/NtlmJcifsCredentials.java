package org.eclipse.mylyn.targetprocess.modules;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.DefaultHttpParamsFactory;
import org.apache.commons.httpclient.params.HttpParams;

/**
 * registers NTLM authentication for apache axis
 * 
 */
public class NtlmJcifsCredentials
{
    public static void register(String password)
    {
        final String username = System.getProperty("user.name");
        final String computername = System.getenv("COMPUTERNAME");
        final String userDomain = System.getenv("USERDOMAIN");
        register(username, password, computername, userDomain);
    }
    public static void register(String username, String password, String userDomain)
    {
        final String computername = getHostname();
        register(username, password, computername, userDomain);
    }
    
    public static String getHostname()
    {
      String hostname = System.getenv().get("HOSTNAME");
      if (hostname!=null && !hostname.trim().equals("")) return hostname;

      // support windows server 2003
      hostname = System.getenv().get("COMPUTERNAME");
      if (hostname!=null && !hostname.trim().equals("")) return hostname;

      return "UNKNOWN-HOST";
    }

    public static void register(
                String username, String password, String computername, String domain)
    {
        final NTCredentials ntCred =
                        new NTCredentials(username, password, computername, domain);

        final CredentialsProvider ntlmCredProvider = new CredentialsProvider()
        {
                public Credentials getCredentials(
                                AuthScheme scheme, String host, int port, boolean proxy)
                                throws CredentialsNotAvailableException
                {
                        return ntCred;
                }
        };
        final DefaultHttpParamsFactory paramFact =
                        new DefaultHttpParamsFactory()
                        {
                                @Override
                                protected HttpParams createParams()
                                {
                                        HttpParams htp = super.createParams();
                                        htp.setParameter(
                                                        CredentialsProvider.PROVIDER,
                                                        ntlmCredProvider);
                                        return htp;
                                }
                        };
        DefaultHttpParams.setHttpParamsFactory(paramFact);

        // we want all our jcifs encoding to be ascii
        jcifs.Config.setProperty("jcifs.encoding", "ASCII");

        // our jcifs implemented NTLM is required for MDW's authentication
        AuthPolicy.registerAuthScheme(AuthPolicy.NTLM, JcifsNtlmScheme.class);
    }
    
    public static void unregister()
    {
    	DefaultHttpParams.setHttpParamsFactory(new DefaultHttpParamsFactory());
    	AuthPolicy.unregisterAuthScheme(AuthPolicy.NTLM);
    }
}
