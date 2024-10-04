
package com.biglybt.plugins.prometheus;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.biglybt.core.util.Debug;
import com.biglybt.pif.PluginException;
import com.biglybt.pif.PluginInterface;
import com.biglybt.pif.UnloadablePlugin;
import com.biglybt.pif.tracker.web.TrackerWebPageRequest;
import com.biglybt.pif.tracker.web.TrackerWebPageResponse;
import com.biglybt.pif.ui.model.BasicPluginConfigModel;
import com.biglybt.ui.webplugin.WebPlugin;

import com.biglybt.core.stats.CoreStats;

public class 
PrometheusPlugin 
	extends WebPlugin
	implements UnloadablePlugin
{
    public static final int DEFAULT_PORT    = 9089;

    private static Properties defaults = new Properties();

    static{

        defaults.put( WebPlugin.PR_DISABLABLE, new Boolean( true ));
        defaults.put( WebPlugin.PR_ENABLE, new Boolean( true ));
        defaults.put( WebPlugin.PR_PORT, new Integer( DEFAULT_PORT ));
        defaults.put( WebPlugin.PR_ROOT_DIR, "web" );
        defaults.put( WebPlugin.PR_ENABLE_KEEP_ALIVE, new Boolean(true));
        defaults.put( WebPlugin.PR_HIDE_RESOURCE_CONFIG, new Boolean(true));
        defaults.put( WebPlugin.PR_PAIRING_SID, "prometheus" );
    }
    
    public
    PrometheusPlugin()
    {
    	super( defaults );
    }
    
	@Override
	public void
	initialize(
		PluginInterface _plugin_interface )
	
		throws PluginException
	{	
		super.initialize( _plugin_interface );
		
		plugin_interface	= _plugin_interface;
		
		plugin_interface.getUtilities().getLocaleUtilities().integrateLocalisedMessageBundle( 
				"com.biglybt.plugins.prometheus.internat.Messages" );
				
		BasicPluginConfigModel	config = getConfigModel();
			
		config.addLabelParameter2( "prometheus.blank" );

		config.addHyperlinkParameter2( "prometheus.openui", getServerURL());
		
		config.addLabelParameter2( "prometheus.blank" );
	}
	
	@Override
	public void
	unload() 
		
		throws PluginException 
	{	

	}
	
	@Override
	public boolean
	generateSupport(
		TrackerWebPageRequest		request,
		TrackerWebPageResponse		response )
	
		throws IOException
	{
		try{
			URL url = request.getAbsoluteURL();
				
			String	url_path = url.getPath();
			
			if ( url_path.equals( "/" ) || url_path.equals( "/metrics" )){
				
					// https://prometheus.io/docs/instrumenting/exposition_formats/
				
				PrintWriter pw = new PrintWriter( new OutputStreamWriter( response.getOutputStream(), "UTF-8" ));
				
				Set<String>	types = new HashSet<>();

				types.add( CoreStats.ST_ALL );

				Map<String,Object>	reply = CoreStats.getStats( types );

				java.util.List<String> keys = new ArrayList<>( reply.keySet());
				
				Collections.sort( keys );
				
				for ( String key: keys ){
					
					Object val = reply.get( key );
					
					if ( val instanceof Number ){
					
						pw.print( "biglybt_" + key.replace( ".", "_" ) + " " + val + "\n");
					}
				}
				
				pw.flush();
				
				response.setContentType( "text/plain; charset=UTF-8" );
				
				response.setGZIP( true );
				
				return( true );
				
				
			}else{
				
				return( super.generateSupport(request, response));
			}
		}catch( Throwable e ){
							
			log( "Processing failed", e );
			
			throw( new IOException( "Processing failed: " + Debug.getNestedExceptionMessage( e )));
		}
	}
}
