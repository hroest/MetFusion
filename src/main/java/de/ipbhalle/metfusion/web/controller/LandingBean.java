/**
 * created by Michael Gerlich, Mar 13, 2013 - 9:59:28 AM

 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package de.ipbhalle.metfusion.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import com.icesoft.faces.component.ext.HtmlInputText;
import com.icesoft.faces.component.ext.HtmlSelectOneMenu;
import com.icesoft.faces.component.ext.HtmlSelectOneRadio;

import de.ipbhalle.enumerations.Adducts;
import de.ipbhalle.enumerations.AvailableParameters;
import de.ipbhalle.metfusion.main.MetFusionBatchSettings;

@ManagedBean
@RequestScoped
public class LandingBean {

	private Map<String, String> map;
	private List<String> keysAsList;
	private Map<AvailableParameters, Object> settings;
	private String message = "";
	
	private final String sepPeakPair = "@";		// separates a peak pair (m/z, int) from another one
	private final String sepPeakInfo = ",";		// separates m/z from int for the same peak pair
	private final String DEFAULT_LINE_SEPARATOR = "\n";	// separates peak pairs as peak list
	private final String DEFAULT_INFO_SEPARATOR = " ";	// separates m/z from int for same peak pair after parsing
	
	private boolean specifiedInstruments = false;
	private boolean specifiedExactMass = false;
	private boolean specifiedParentIon = false;
	private boolean specifiedAdduct = false;
	private boolean specifiedPeaks = false;
	
	
	public LandingBean() {
		FacesContext fc = FacesContext.getCurrentInstance();
		map = fc.getExternalContext().getRequestParameterMap();
		keysAsList = new ArrayList<String>(map.keySet());
		settings = new HashMap<AvailableParameters, Object>();
		
		parseSettings();
		
		pushSettings();
	}

	/**
	 * parse encoded settings
	 */
	private void parseSettings() {
		AvailableParameters[] ap = AvailableParameters.values();
		for (int i = 0; i < ap.length; i++) {
			String param = ap[i].toString();
			if(map.containsKey(param)) {
				// peaks richtig parsen mit Komma und @
				if(AvailableParameters.peaks.toString().equals(param)) {
					String peaks = map.get(param);	// single line should contain m/z and intensity separated by comma and peak-pairs separated by @
					peaks = peaks.replaceAll(sepPeakPair, DEFAULT_LINE_SEPARATOR);
					peaks = peaks.replaceAll(sepPeakInfo, DEFAULT_INFO_SEPARATOR);
					specifiedPeaks = true;
					settings.put(AvailableParameters.peaks, peaks);
				}
				else if(AvailableParameters.mbInstruments.toString().equals(param)) {	// Instrumentenliste mit Komma
					String instruments = map.get(param);
					//String[] ins = instruments.split(",");
					specifiedInstruments = true;
					settings.put(AvailableParameters.mbInstruments, instruments);
				}
				else if(AvailableParameters.mfExactMass.toString().equals(param)) {
					specifiedExactMass = true;
					settings.put(AvailableParameters.mfExactMass, map.get(param));
				}
				else if(AvailableParameters.mfParentIon.toString().equals(param)) {
					specifiedParentIon = true;
					settings.put(AvailableParameters.mfParentIon, map.get(param));
				}
				else if(AvailableParameters.mfAdduct.toString().equals(param)) {
					specifiedAdduct = true;
					settings.put(AvailableParameters.mfAdduct, map.get(param));
				}
				else settings.put(ap[i], map.get(param));
				
				// boolean werte mit true/false
			}
		}
	}

	/**
	 * push stored settings to MetFusionBean
	 */
	public void pushSettings() {
		FacesContext fc = FacesContext.getCurrentInstance();
		ELContext elc = fc.getELContext();
		ELResolver el = fc.getApplication().getELResolver();
		/** retrieve session appBean */
		MetFusionBean appBean = (MetFusionBean) el.getValue(elc, null, "appBean");
		
		MetFusionBatchSettings mfbs = new MetFusionBatchSettings();
		mfbs.loadSettingsForLandingPage(settings);
		// MassBank settings
		appBean.getMblb().setCutoff(mfbs.getMbCutoff());
		if(specifiedInstruments) {
			appBean.getMblb().setSelectedInstruments(new String[]{});
			appBean.getMblb().loadInstruments(new String[]{});
		}
		appBean.getMblb().setSelectedInstruments(mfbs.getSelectedInstruments());
		appBean.getMblb().loadInstruments(mfbs.getSelectedInstruments());
		// set ionization and update MetFrag related ionization mode via ValueChangeEvent
		appBean.getMblb().setSelectedIon(String.valueOf(mfbs.getMbIonization().getValue()));
		ValueChangeEvent vce = new ValueChangeEvent(new HtmlSelectOneMenu(), 	// HtmlSelectOneMenu corresponds to Ionization Mode selection in MassBank bean
				appBean.getMblb().getSelectedIon(), appBean.getMblb().getSelectedIon());
		appBean.changeIonizationListener(vce);		// update ionization mode
		
		appBean.getMblb().setLimit(mfbs.getMbLimit());
		appBean.getMblb().setUniqueInchi(mfbs.isUnique());
		// TODO: MassBank MS level
		
		if(!specifiedPeaks)	{		// no peaks specified
			message = "<b>Missing peak information</b>, please specify peaks as pairs of m/z and relative intensity, " +
					"e.g. 147.044,607@153.019,999@273.076,999 with the <b>" + AvailableParameters.peaks + "</b> parameter.";
			
			fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "failed");
			return;
		}
		// MetFrag settings
		if(specifiedAdduct)
			appBean.getMfb().setSelectedAdduct(mfbs.getMfAdduct().getDifference());
		if(specifiedParentIon)
			appBean.getMfb().setParentIon(mfbs.getMfParentIon());
		if(specifiedExactMass && !specifiedAdduct && !specifiedParentIon) {		// default to neutral adduct and exact mass equals parent ion if not specified
			appBean.getMfb().setExactMass(mfbs.getMfExactMass());
			appBean.getMfb().setParentIon(mfbs.getMfExactMass());
			appBean.getMfb().setSelectedAdduct(Adducts.Neutral.getDifference());
		}
		else {
			appBean.getMfb().setExactMass(mfbs.getMfExactMass());
		}
		if((!specifiedExactMass && !specifiedAdduct && !specifiedParentIon)) {
			message = "<b>Missing value for exact mass or adduct and parent ion.</b> Please set appropriate parameters <b>" + AvailableParameters.mfExactMass +
					", " + AvailableParameters.mfAdduct + " and/or " + AvailableParameters.mfParentIon + "</b>.";
			
			fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "failed");
			return;
//			try {
//				fc.getExternalContext().redirect("http://msbi.ipb-halle.de/MetFusion/LandingPageError.xhtml");
//				return;
//			} catch (IOException e1) {
//				System.err.println("Error redirecting from LandingPage to LandingPageError due to missing values for exact mass or parent ion.");
//				return;
//			}
		}
		appBean.getMfb().calculateExactMass();
		
		appBean.getMfb().setSelectedDB(mfbs.getMfDatabase().toString());
		ValueChangeEvent db = new ValueChangeEvent(new HtmlSelectOneRadio(), 	// HtmlSelectOneRadio corresponds to Upstream DB selection in MetFrag bean
					mfbs.getMfDatabase().toString(), mfbs.getMfDatabase().toString());
		appBean.getMfb().changeDatabase(db);		// update selected compound database
		appBean.getMfb().setDatabaseID(mfbs.getMfDatabaseIDs());
		appBean.getMfb().setMolecularFormula(mfbs.getMfFormula());
		ValueChangeEvent formula = new ValueChangeEvent(new HtmlInputText(), mfbs.getMfFormula(), mfbs.getMfFormula());
		appBean.getMfb().changeFormula(formula);		// update molecular formula and mass 
		appBean.getMfb().setLimit(mfbs.getMfLimit());
		appBean.getMfb().setMzabs(mfbs.getMfMZabs());
		appBean.getMfb().setMzppm(mfbs.getMfMZppm());
		appBean.getMfb().setSearchppm(mfbs.getMfSearchPPM());
		appBean.getMfb().setOnlyCHNOPS(mfbs.isOnlyCHNOPS());
		appBean.getMfb().setUniqueInchi(mfbs.isUnique());
		
		// MetFusion settings
		appBean.setUseClustering(mfbs.isClustering());
		appBean.setUseInChIFiltering(mfbs.isUnique());
		appBean.setInputSpectrum(mfbs.getPeaks());
		
		// navigate to main page with set settings
//		fc.getApplication().getNavigationHandler().handleNavigation(fc, null, "success");
		try {
			fc.getExternalContext().redirect("http://msbi.ipb-halle.de/MetFusion/");
		} catch (IOException e) {
			System.err.println("Error redirecting from LandingPage to main page.");
			try {
				fc.getExternalContext().redirect("http://msbi.ipb-halle.de/MetFusion/LandingPageError.xhtml");
			} catch (IOException e1) {
				System.err.println("Error redirecting from LandingPage to LandingPageError.");
			}
		}
	}

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}
	
	public List<String> getKeysAsList() {
		return keysAsList;
		 //return new ArrayList<String>(map.keySet());
	}

	public void setKeysAsList(List<String> keysAsList) {
		this.keysAsList = keysAsList;
	}

	public Map<AvailableParameters, Object> getSettings() {
		return settings;
	}

	public void setSettings(Map<AvailableParameters, Object> settings) {
		this.settings = settings;
	}

	public boolean isSpecifiedInstruments() {
		return specifiedInstruments;
	}

	public void setSpecifiedInstruments(boolean specifiedInstruments) {
		this.specifiedInstruments = specifiedInstruments;
	}

	public boolean isSpecifiedExactMass() {
		return specifiedExactMass;
	}

	public void setSpecifiedExactMass(boolean specifiedExactMass) {
		this.specifiedExactMass = specifiedExactMass;
	}

	public boolean isSpecifiedParentIon() {
		return specifiedParentIon;
	}

	public void setSpecifiedParentIon(boolean specifiedParentIon) {
		this.specifiedParentIon = specifiedParentIon;
	}

	public boolean isSpecifiedAdduct() {
		return specifiedAdduct;
	}

	public void setSpecifiedAdduct(boolean specifiedAdduct) {
		this.specifiedAdduct = specifiedAdduct;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSpecifiedPeaks() {
		return specifiedPeaks;
	}

	public void setSpecifiedPeaks(boolean specifiedPeaks) {
		this.specifiedPeaks = specifiedPeaks;
	}
}
