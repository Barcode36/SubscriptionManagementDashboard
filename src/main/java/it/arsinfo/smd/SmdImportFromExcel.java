package it.arsinfo.smd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import it.arsinfo.smd.data.Anno;
import it.arsinfo.smd.data.AreaSpedizione;
import it.arsinfo.smd.data.Diocesi;
import it.arsinfo.smd.data.Paese;
import it.arsinfo.smd.data.Provincia;
import it.arsinfo.smd.data.TitoloAnagrafica;
import it.arsinfo.smd.entity.Abbonamento;
import it.arsinfo.smd.entity.Anagrafica;

public class SmdImportFromExcel {
    private static final Logger log = LoggerFactory.getLogger(Smd.class);

    public static final String ELENCO_ABBONATI = "data/ELENCOABBONATI2020-060919.xls";
    public static final String ARCHIVIO_CLIENTI = "data/ARCHIVIOCLIENTI-11092019.xls";
    public static final String CAMPAGNA_2020 = "data/CA2020COMPLETA.xls";
    public static final String ABBONATI_ESTERO = "data/ABBONATIESTERO2020.xls";
    public static final String ABBONATI_ITA_ESTERO = "data/ABBONATITALIABENEFESTERO-10092019.xls";
    public static final String BENEFICIARI_2020 = "data/BENEFICIARI2020Exported.xls";
    public static final String OMAGGIO_MESSAGGIO = "data/OMAGGIOMESSAGGIO2020.xls";

    public static Integer getRowInteger(String value) {
        value = value.trim();
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
        
    }
    
    public BigDecimal getRowBigDecimal(String value) {
        value = value.trim().replace(",", ".");
        if (StringUtils.isEmpty(value)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim().replace(",", "."));
    }
    

    public static AreaSpedizione getAreaSpedizione(String annazion) {
        if (annazion.equals("RSM") || annazion.equals("ITA") || annazion.equals("CVC")) {
                return AreaSpedizione.Italia;
        } else if (annazion.equals("GBR") || annazion.equals("FRA")
                    || annazion.equals("ROM") || annazion.equals("CZE")
                    || annazion.equals("HRV") || annazion.equals("LTU")
                    || annazion.equals("LTU") || annazion.equals("NLD")
                    || annazion.equals("LUX") || annazion.equals("CHE")
                    || annazion.equals("POL") || annazion.equals("ALB")
                    || annazion.equals("DEU") || annazion.equals("ESP")
                    || annazion.equals("CYP") || annazion.equals("BEL")
                    || annazion.equals("SVN") || annazion.equals("IRL")
                    || annazion.equals("ISR") || annazion.equals("PRT")
                    || annazion.equals("SYR") || annazion.equals("EGY")
                    || annazion.equals("TUR") || annazion.equals("LBY")
                    || annazion.equals("GRC")) {
            return AreaSpedizione.EuropaBacinoMediterraneo;
        } 
        return AreaSpedizione.AmericaAfricaAsia;

    }
    public static String getCapFromAnlocali(String anlocali) {
        return anlocali.substring(0,5);
    }

    public static String getCittaFromAnlocali(String anlocali) {
        if (anlocali.contains("(")) {
            return anlocali.substring(6,anlocali.indexOf("(")-1);
        }
        return anlocali.substring(6);
    }

    public static String getProvinciaFromAnlocali(String anlocali) {
        if (anlocali.contains("(") && anlocali.contains(")")) {
            return anlocali.substring(anlocali.indexOf("(")+1,anlocali.indexOf(")"));
        }
        if (anlocali.contains("(")) {
            return anlocali.substring(anlocali.indexOf("(")+1);
        }
        return "";
    }

    public static Provincia getProvincia(String sigla) {
        try {
            return Provincia.valueOf(sigla);
        } catch (Exception e) {
            return Provincia.ND;
        }
    }

    public static Anagrafica getAnagraficaByAncodcon(String pncodcon) throws UnsupportedOperationException {
        Anagrafica a = new Anagrafica();
        if (pncodcon.length() == 10) {
            if (pncodcon.startsWith("E")) {
                a.setCodeLineBase("10000" + pncodcon.substring(1));
            } else {
                a.setCodeLineBase("0000" + pncodcon);
            }
        } else if (pncodcon.length() == 14){
            a.setCodeLineBase(pncodcon);
        }
        String codeLine = Abbonamento.generaCodeLine(Anno.ANNO2019,
                                                     a);
        if (!Abbonamento.checkCodeLine(codeLine)) {
            log.warn("----- codeLine invalid ------");
            log.warn("ANCODICE: " + pncodcon);
            log.warn("------------------------------");
            throw new UnsupportedOperationException();
        }
        return a;
    }
    
    public static void populateAnagraficaEstero(Anagrafica a, String ancodice, String destitolo,String andescri, String andescr2, String anindiri, String anindir1,
            String anlocali,String annazion) {
        a.setTitolo(TitoloAnagrafica.getByIntestazione(destitolo));        
        if (a.getTitolo() == TitoloAnagrafica.Nessuno) {
            log.warn("-----Anagrafica Titolo error------");
            log.warn(a.getCodeLineBase());
            log.warn("------------------------------");
            
            throw new UnsupportedOperationException();
        }
        a.setDenominazione(andescri);
        a.setNome(andescr2);
        a.setIndirizzo(anindiri);
        a.setCitta(anlocali);
        a.setDiocesi(Diocesi.DIOCESI000);
        a.setPaese(Paese.getBySigla(annazion));
        if (a.getPaese() == Paese.ND) {
            log.warn("-----Paese non Definito------");
            log.warn(ancodice);
            log.warn(andescri);
            log.warn(anindiri);
            log.warn(anlocali);
            log.warn(annazion);
            log.warn("------------------------------");
            
            throw new UnsupportedOperationException();
        }
        a.setAreaSpedizione(getAreaSpedizione(annazion));
    }
        
    public static void populateAnagraficaCampagna(Anagrafica a, String destitolo,
            String andescri, String andescr2, String anindiri, String anindir1,
            String anlocali) throws UnsupportedOperationException {

        a.setTitolo(TitoloAnagrafica.getByIntestazione(destitolo));        
        
        if (a.getTitolo() == TitoloAnagrafica.Nessuno) {
            log.warn("-----Anagrafica Titolo error------");
            log.warn(a.getCodeLineBase());
            log.warn(a.getCitta());
            log.warn(a.getCap());
            log.warn(a.getProvincia().getNome());
            log.warn("------------------------------");
            
            throw new UnsupportedOperationException();
        }

        a.setDenominazione(andescri);
        a.setNome(andescr2);
        
        a.setCitta(getCittaFromAnlocali(anlocali));
        a.setCap(getCapFromAnlocali(anlocali));
        a.setProvincia(getProvincia(getProvinciaFromAnlocali(anlocali)));
        
        try {
            Integer.getInteger(a.getCap());
        } catch (Exception e) {
            log.warn("-----Anagrafica CAP error------");
            log.warn(a.getCodeLineBase());
            log.warn(a.getCitta());
            log.warn(a.getCap());
            log.warn(a.getProvincia().getNome());
            log.warn("------------------------------");
            
            throw new UnsupportedOperationException();
        }

        
        a.setIndirizzo(anindiri);
        if (!anindir1.equals("")) {
            if (a.getCodeLineBase().equals("00000000012956")) {
                a.setIndirizzoSecondaRiga(null);
            } else if (a.getCodeLineBase().equals("00000000066055")) {
                    a.setIndirizzo("FRAZ.VIGHIZZOLO - VIA S. GIOVANNI 255");
            } else if (a.getCodeLineBase().equals("00000000020992")) {
                a.setIndirizzo("LOC.S.PIETRO DI BARBOZZA-STR.CHIESA 2");
            } else if (a.getCodeLineBase().equals("00000000061880")) {
                a.setIndirizzo("LOC.MAGLIO DI SOPRA - VIA MARZOTTO 2");
            } else {
                a.setIndirizzoSecondaRiga(anindir1);
            }
            log.debug("-----Fixed ANINDIR1------");
            log.debug(a.getCodeLineBase());
            log.debug("ANINDIRI: " + anindiri);
            log.debug("ANINDIR1: " + anindir1);
            log.debug("Indirizzo1: " + a.getIndirizzo());
            log.debug("Indirizzo2: " + a.getIndirizzoSecondaRiga());
            log.debug("------------------------------");
            
        }
                       
        if (a.getProvincia() == Provincia.ND) {
            if (a.getCodeLineBase().equals("00000000015153")) {
                a.setProvincia(Provincia.RM);
                log.debug("-----Anagrafica Provincia RM------");
                log.debug(a.getCodeLineBase());
                log.debug(a.getCitta());
                log.debug(a.getCap());
                log.debug(a.getProvincia().getNome());
                log.debug("------------------------------");
                
            } else if (a.getCodeLineBase().equals("00000000070340")) {
                a.setProvincia(Provincia.TV);
                log.debug("-----Anagrafica Provincia TV------");
                log.debug(a.getCodeLineBase());
                log.debug(a.getCitta());
                log.debug(a.getCap());
                log.debug(a.getProvincia().getNome());
                log.debug("------------------------------");
                
            } else if (a.getCap().equals("87020")) {
                a.setProvincia(Provincia.CS);
                log.debug("-----Anagrafica Provincia CS------");
                log.debug(a.getCodeLineBase());
                log.debug(a.getCitta());
                log.debug(a.getCap());
                log.debug(a.getProvincia().getNome());
                log.debug("------------------------------");
                                
            } else if (a.getCap().equals("31048")) {
                a.setProvincia(Provincia.TV);
                log.debug("-----Anagrafica Provincia TV------");
                log.debug(a.getCodeLineBase());
                log.debug(a.getCitta());
                log.debug(a.getCap());
                log.debug(a.getProvincia().getNome());
                log.debug("------------------------------");
                                
            } else if (a.getCap().equals("35010")) {
                a.setProvincia(Provincia.PD);
                log.debug("-----Anagrafica Provincia PD------");
                log.debug(a.getCodeLineBase());
                log.debug(a.getCitta());
                log.debug(a.getCap());
                log.debug(a.getProvincia().getNome());
                log.debug("------------------------------");
                                
            } else if (a.getCap().equals("41033")) {
                a.setProvincia(Provincia.MO);
                log.debug("-----Anagrafica Provincia MO------");
                log.debug(a.getCodeLineBase());
                log.debug(a.getCitta());
                log.debug(a.getCap());
                log.debug(a.getProvincia().getNome());
                log.debug("------------------------------");
                                
            } else if (a.getCap().equals("47893")) {
                a.setPaese(Paese.SM);
                a.setCitta("BORGO MAGGIORE");
                a.setDiocesi(Diocesi.DIOCESI175);
                log.debug("-----Anagrafica Paese San Marino------");
                log.debug(a.getCodeLineBase());
                log.debug(a.getCitta());
                log.debug(a.getCap());
                log.debug(a.getPaese().getNome());
                log.debug(a.getDiocesi().getDetails());
                log.debug("------------------------------");
                                
            } else if (a.getCap().equals("50038")) {
                a.setProvincia(Provincia.FI);
                log.debug("-----Anagrafica Provincia FI------");
                log.debug(a.getCodeLineBase());
                log.debug(a.getCitta());
                log.debug(a.getCap());
                log.debug(a.getProvincia().getNome());
                log.debug("------------------------------");
                                
            } else if (a.getCap().equals("54028")) {
                a.setProvincia(Provincia.MS);
                log.debug("-----Anagrafica Provincia MS------");
                log.debug(a.getCodeLineBase());
                log.debug(a.getCitta());
                log.debug(a.getCap());
                log.debug(a.getProvincia().getNome());
                log.debug("------------------------------");
                                
            } else if (a.getCap().equals("56035")) {
                a.setProvincia(Provincia.PI);
                log.debug("-----Anagrafica Provincia PI------");
                log.debug(a.getCodeLineBase());
                log.debug(a.getCitta());
                log.debug(a.getCap());
                log.debug(a.getProvincia().getNome());
                log.debug("------------------------------");
                
            } else if (a.getCap().equals("87064")) {
                a.setProvincia(Provincia.CS);
                log.debug("-----Anagrafica Provincia CS------");
                log.debug(a.getCodeLineBase());
                log.debug(a.getCitta());
                log.debug(a.getCap());
                log.debug(a.getProvincia().getNome());
                log.debug("------------------------------");
                
            } else if (a.getCap().equals("60012")) {
                a.setProvincia(Provincia.AN);
                a.setIndirizzo(a.getIndirizzo()+ " FRAZ.BRUGNETTO");
                log.debug("-----Anagrafica Provincia AN------");
                log.debug(a.getCodeLineBase());
                log.debug(a.getIndirizzo());
                log.debug(a.getCitta());
                log.debug(a.getCap());
                log.debug(a.getProvincia().getNome());
                log.debug("------------------------------");
                                
            } else if (a.getCap().equals("80061")) {
                a.setProvincia(Provincia.NA);
                log.debug("-----Anagrafica Provincia NA------");
                log.debug(a.getCodeLineBase());
                log.debug(a.getIndirizzo());
                log.debug(a.getCitta());
                log.debug(a.getCap());
                log.debug(a.getProvincia().getNome());
                log.debug("------------------------------");
                                
            } else {
                log.warn("-----Anagrafica Provincia error------");
                log.warn(a.getCodeLineBase());
                log.warn(a.getCitta());
                log.warn(a.getCap());
                log.warn(a.getProvincia().getNome());
                log.warn("------------------------------");
                
                throw new UnsupportedOperationException();
            }
        }

    }
    
    public Integer processRowCampagnaMessaggioNum(Row row) {
        DataFormatter dataFormatter = new DataFormatter();
        return getRowInteger(dataFormatter.formatCellValue(row.getCell(8)));
    }    
    public BigDecimal processRowCampagnaMessaggioCosto(Row row) {
        DataFormatter dataFormatter = new DataFormatter();
        return getRowBigDecimal(dataFormatter.formatCellValue(row.getCell(9)));
    }
    
    public Integer processRowCampagnaBlocchettiNum(Row row) {
        DataFormatter dataFormatter = new DataFormatter();
        return getRowInteger(dataFormatter.formatCellValue(row.getCell(10)));
    }
    
    public BigDecimal processRowCampagnaBlocchettiCosto(Row row) {
        DataFormatter dataFormatter = new DataFormatter();
        return getRowBigDecimal(dataFormatter.formatCellValue(row.getCell(11)));
    }
    public Integer processRowCampagnaManifestiNum(Row row) {
        DataFormatter dataFormatter = new DataFormatter();
        return getRowInteger(dataFormatter.formatCellValue(row.getCell(12)));
    }
    public BigDecimal processRowCampagnaManifestiCosto(Row row) {
        DataFormatter dataFormatter = new DataFormatter();
        return getRowBigDecimal(dataFormatter.formatCellValue(row.getCell(13)));
    }        
    public Integer processRowCampagnaLodareNum(Row row) {
        DataFormatter dataFormatter = new DataFormatter();
        return getRowInteger(dataFormatter.formatCellValue(row.getCell(14)));
    }

    public BigDecimal processRowCampagnaLodareCosto(Row row) {
        DataFormatter dataFormatter = new DataFormatter();
        return getRowBigDecimal(dataFormatter.formatCellValue(row.getCell(15)));
    }

    public BigDecimal processRowCampagnaTotaleCosto(Row row) throws UnsupportedOperationException {
        DataFormatter dataFormatter = new DataFormatter();
        BigDecimal totimp = getRowBigDecimal(dataFormatter.formatCellValue(row.getCell(16)));
        BigDecimal totabb = getRowBigDecimal(dataFormatter.formatCellValue(row.getCell(22)));
        if ( totabb.compareTo(totimp) != 0) {
            throw new UnsupportedOperationException();
        }

        return totimp;
    }

    public String processRowCampagnaCodeline(Row row,String pncodcon) throws UnsupportedOperationException {
        DataFormatter dataFormatter = new DataFormatter();
        String pndessup = dataFormatter.formatCellValue(row.getCell(7));
        if (pncodcon.equals("0000004967")) {
            pndessup="000000020000730517";
            log.debug("----- codeLine fix ------");
            log.debug("ANCODICE: " + pncodcon);
            log.debug("codeLine: " + pndessup);
            log.debug("------------------------------");
            
        } else if (pncodcon.equals("0000048374")) {
            pndessup="000000020000730416";
            log.debug("----- codeLine fix ------");
            log.debug("ANCODICE: " + pncodcon);
            log.debug("codeLine: " + pndessup);
            log.debug("------------------------------");
            
        }
        if (!Abbonamento.checkCodeLine(pndessup)) {
            log.warn("----- codeLine invalid ------");
            log.warn("ANCODICE: " + pncodcon);
            log.warn("------------------------------");
            
            throw new UnsupportedOperationException();
        }
        return pndessup;
        
    }
    
    public Map<String,Row> getOmaggioMessaggio2020() throws IOException {
        DataFormatter dataFormatter = new DataFormatter();
        File ca2020 = new File(OMAGGIO_MESSAGGIO);
        Workbook wbca2020 = new HSSFWorkbook(new FileInputStream(ca2020));
        Map<String,Row> rows = new HashMap<>();
        for (Row row: wbca2020.getSheetAt(0)) {
            String nominativo = dataFormatter.formatCellValue(row.getCell(2));
            if (nominativo.endsWith("NOMINATIVO")) {
                continue;
            }
            rows.put(nominativo,row);
        }
        return rows;
    }
    
    public Map<String,Anagrafica> importOmaggioMessaggio2020(Map<String, Row> campagnaRows) throws IOException {        
        DataFormatter dataFormatter = new DataFormatter();
        Set<String> errors = new HashSet<>();
        Map<String, Anagrafica> anagraficaMap = new HashMap<>();
        for (String nominativo : campagnaRows.keySet() ) {
            try {
                Anagrafica a = processRowOmaggio(campagnaRows.get(nominativo),nominativo,dataFormatter);
                anagraficaMap.put(a.getCodeLineBase(), a);                
            } catch (UnsupportedOperationException e) {
                errors.add(nominativo);
                continue;
            }
        }
        log.debug("Omaggio Messaggio 2020 -  Errori Trovati: "
                + errors.size());
        log.debug("Omaggio Messaggio 2020 -  Clienti Trovati: "
                + anagraficaMap.size());
        return anagraficaMap;
    }

    public Integer getQuantitaFromOmaggioRow(Row row) {
        DataFormatter dataFormatter = new DataFormatter();
        return getRowInteger(dataFormatter.formatCellValue(row.getCell(0)));
    }
    
    private Anagrafica processRowOmaggio(Row row, String nominativo, DataFormatter dataFormatter) throws UnsupportedOperationException {

        String cap = dataFormatter.formatCellValue(row.getCell(1));
        String titolo = dataFormatter.formatCellValue(row.getCell(3));
        String sottointes = dataFormatter.formatCellValue(row.getCell(4));
        String indirizzo = dataFormatter.formatCellValue(row.getCell(5));
        String citta = dataFormatter.formatCellValue(row.getCell(6));
        String provincia = dataFormatter.formatCellValue(row.getCell(7));
        String via_aerea = dataFormatter.formatCellValue(row.getCell(8));
        if (!via_aerea.equals("N")) {
            log.warn("via_aerea mismatch: " + via_aerea);
            throw new UnsupportedOperationException();
        }
        
        String paese = dataFormatter.formatCellValue(row.getCell(9));
        String abstato = dataFormatter.formatCellValue(row.getCell(10));
        if (!abstato.equals("OM")) {
            log.warn("abstato mismatch: " + abstato);
            throw new UnsupportedOperationException();
        }
        Anagrafica anagrafica = getAnagraficaByAncodcon(Anagrafica.generaCodeLineBase());
        anagrafica.setCap(cap);
        anagrafica.setTitolo(TitoloAnagrafica.getByIntestazione(titolo));
        anagrafica.setNome(sottointes);
        anagrafica.setDenominazione(nominativo);
        anagrafica.setIndirizzo(indirizzo);
        anagrafica.setCitta(citta);
        anagrafica.setProvincia(getProvincia(provincia));
        anagrafica.setPaese(Paese.getByNome(paese));
        anagrafica.setAreaSpedizione(getAreaSpedizione(anagrafica.getPaese().getSigla()));
        
                
        
        return anagrafica;

    }

    public Map<String,Row> getCampagna2020() throws IOException {
        DataFormatter dataFormatter = new DataFormatter();
        File ca2020 = new File(CAMPAGNA_2020);
        Workbook wbca2020 = new HSSFWorkbook(new FileInputStream(ca2020));
        Map<String,Row> rows = new HashMap<>();
        for (Row row: wbca2020.getSheetAt(0)) {
            String pncodcon = dataFormatter.formatCellValue(row.getCell(0));
            if (pncodcon.endsWith("pncodcon")) {
                continue;
            }
            rows.put(pncodcon,row);
        }
        return rows;
    }
    
    public Map<String,Anagrafica> importCampagna2020(Map<String, Row> campagnaRows) throws IOException {        
        DataFormatter dataFormatter = new DataFormatter();
        
        Set<String> errors = new HashSet<>();
        Map<String, Anagrafica> anagraficaMap = new HashMap<>();
        for (String pncodcon : campagnaRows.keySet() ) {
            try {
                anagraficaMap.put(pncodcon, processRowCampagna(campagnaRows.get(pncodcon),pncodcon,dataFormatter));                
            } catch (UnsupportedOperationException e) {
                errors.add(pncodcon);
                continue;
            }
        }
        log.debug("Campagna 2020 -  Errori Trovati: "
                + errors.size());
        log.debug("Campagna 2020 -  Clienti Trovati: "
                + anagraficaMap.size());
        return anagraficaMap;
    }
     
    private Anagrafica processRowCampagna(Row row, String pncodcon, DataFormatter dataFormatter) throws UnsupportedOperationException {

        String destitolo = dataFormatter.formatCellValue(row.getCell(1));
        String andescri = dataFormatter.formatCellValue(row.getCell(2));
        String andescr2 = dataFormatter.formatCellValue(row.getCell(3));
        String anindiri = dataFormatter.formatCellValue(row.getCell(4));
        String anindir1 = dataFormatter.formatCellValue(row.getCell(5));
        String anlocali = dataFormatter.formatCellValue(row.getCell(6));
        Anagrafica anagrafica = getAnagraficaByAncodcon(pncodcon);
        
        populateAnagraficaCampagna(anagrafica, destitolo, andescri, andescr2, anindiri, anindir1, anlocali);
                
        String scopro =        dataFormatter.formatCellValue(row.getCell(17));
        if (!scopro.equals("")) {
            log.warn("scopro mismatch: " + pncodcon);
            throw new UnsupportedOperationException();
        }
        
        return anagrafica;

    }
    
    public Map<String,Anagrafica> importArchivioClienti() throws IOException {
        DataFormatter dataFormatter = new DataFormatter();

        File ac = new File(ARCHIVIO_CLIENTI);
        Workbook wbac = new HSSFWorkbook(new FileInputStream(ac));

        Set<String> errors = new HashSet<>();
        Map<String, Anagrafica> anagraficaMap = new HashMap<>();
        for (Row row : wbac.getSheetAt(0)) {
            String picoddio = dataFormatter.formatCellValue(row.getCell(0));
            String ancodice = dataFormatter.formatCellValue(row.getCell(1));
            if (picoddio.equalsIgnoreCase("PICODDIO")) {
                continue;
            }
            try {
                anagraficaMap.put(ancodice, processRowAnagrafica(row, picoddio,ancodice, dataFormatter));
            } catch (UnsupportedOperationException e) {
                errors.add(ancodice);
                continue;
            }
        }

        log.debug("Archivio Clienti Errori Trovati: "
                + errors.size());
        log.debug("Archivio Clienti Trovati: "
                + anagraficaMap.size());
        
        return anagraficaMap;
    }

    public Map<String,Anagrafica> importElencoAbbonati() throws IOException {
        DataFormatter dataFormatter = new DataFormatter();

        File ac = new File(ELENCO_ABBONATI);
        Workbook wbac = new HSSFWorkbook(new FileInputStream(ac));

        Set<String> errors = new HashSet<>();
        Map<String, Anagrafica> anagraficaMap = new HashMap<>();
        for (Row row : wbac.getSheetAt(0)) {
            String picoddio = dataFormatter.formatCellValue(row.getCell(0));
            String ancodice = dataFormatter.formatCellValue(row.getCell(1));
            if (picoddio.equalsIgnoreCase("PICODDIO")) {
                continue;
            }
            try {
                anagraficaMap.put(ancodice, processRowAnagrafica(row, picoddio,ancodice, dataFormatter));
            } catch (UnsupportedOperationException e) {
                errors.add(ancodice);
                continue;
            }
        }

        log.debug("Elenco Abbonanti Errori Trovati: "
                + errors.size());
        log.debug("Elenco Abbonati Trovati: "
                + anagraficaMap.size());
        
        return anagraficaMap;
    }
    
    public List<Row> getBeneficiari() throws IOException {
        DataFormatter dataFormatter = new DataFormatter();

        File ac = new File(BENEFICIARI_2020);
        Workbook wbac = new HSSFWorkbook(new FileInputStream(ac));

        List<Row> rows = new ArrayList<Row>();
        for (Row row : wbac.getSheetAt(0)) {
            String antipcon = dataFormatter.formatCellValue(row.getCell(0));
            if (antipcon.equalsIgnoreCase("antipcon")) {
                continue;
            }
            rows.add(row);
        }            
        return rows;
    }
    
    public BigDecimal getPrezzoFromRowBeneficiari(Row row) {
        DataFormatter dataFormatter = new DataFormatter();
        return getRowBigDecimal(dataFormatter.formatCellValue(row.getCell(11)));
    }

    public Integer getQuantitaFromRowBeneficiari(Row row) {
        DataFormatter dataFormatter = new DataFormatter();
        return getRowInteger(dataFormatter.formatCellValue(row.getCell(14)));
    }

    public Map<String,Anagrafica> importBeneficiari(List<Row> rows) throws IOException {
        DataFormatter dataFormatter = new DataFormatter();

        Set<String> errors = new HashSet<>();
        Map<String, Anagrafica> anagraficaMap = new HashMap<>();
        for (Row row : rows) {
            String ancodice = dataFormatter.formatCellValue(row.getCell(1));
            String andescri = dataFormatter.formatCellValue(row.getCell(2));
            String anindiri = dataFormatter.formatCellValue(row.getCell(3));
            String anindir2 = dataFormatter.formatCellValue(row.getCell(4));
            String an___cap = dataFormatter.formatCellValue(row.getCell(5));
            String anlocali = dataFormatter.formatCellValue(row.getCell(6));
            String anprovin = dataFormatter.formatCellValue(row.getCell(7));
            String annazion = dataFormatter.formatCellValue(row.getCell(8));
            String bancodice  = dataFormatter.formatCellValue(row.getCell(15));
            String bandescri = dataFormatter.formatCellValue(row.getCell(16));
            String banindiri = dataFormatter.formatCellValue(row.getCell(17));
            String banindir2 = dataFormatter.formatCellValue(row.getCell(18));
            String ban___cap = dataFormatter.formatCellValue(row.getCell(19));
            String banlocali = dataFormatter.formatCellValue(row.getCell(20));
            String banprovin = dataFormatter.formatCellValue(row.getCell(21));
            String bannazion = dataFormatter.formatCellValue(row.getCell(22));
            Anagrafica abene = getAnagraficaByAncodcon(ancodice);
            abene.setDenominazione(andescri);
            abene.setIndirizzo(anindiri);
            abene.setIndirizzoSecondaRiga(anindir2);
            abene.setCitta(anlocali);
            if (!an___cap.trim().equals("")) {
                abene.setCap(an___cap);
            }
            abene.setProvincia(getProvincia(anprovin));
            abene.setPaese(Paese.getBySigla(annazion));
            abene.setAreaSpedizione(getAreaSpedizione(annazion));
            
            anagraficaMap.put(ancodice, abene);
            
            if (bancodice.trim().equals("")) {
                continue;
            }
            Anagrafica ainte = getAnagraficaByAncodcon(bancodice);
            ainte.setDenominazione(bandescri);
            ainte.setIndirizzo(banindiri);
            ainte.setIndirizzoSecondaRiga(banindir2);
            ainte.setCitta(banlocali);
            if (!ban___cap.trim().equals("")) {
                ainte.setCap(ban___cap);
            }
            ainte.setProvincia(getProvincia(banprovin));
            ainte.setPaese(Paese.getBySigla(bannazion));
            ainte.setAreaSpedizione(getAreaSpedizione(bannazion));
            anagraficaMap.put(bancodice, ainte);
        }

        log.debug("Beneficiari Ita Errori Trovati: "
                + errors.size());
        log.debug("Beneficiari Ita Trovati: "
                + anagraficaMap.size());
        
        return anagraficaMap;
    }

    public List<Row> getAbbonatiItaEstero() throws IOException{
        DataFormatter dataFormatter = new DataFormatter();

        File ac = new File(ABBONATI_ITA_ESTERO);
        Workbook wbac = new HSSFWorkbook(new FileInputStream(ac));
        List<Row> rows = new ArrayList<>();
        for (Row row : wbac.getSheetAt(0)) {
            String ancodiceI = dataFormatter.formatCellValue(row.getCell(0));
            if (ancodiceI.equals("COD.")) {
                continue;
            }
            if (ancodiceI.trim().equalsIgnoreCase("")) {
                break;
            }
            rows.add(row);
        }
       
        return rows;
    }

    //FIXME
    public Map<String,Anagrafica> importAbbonatiItaEstero(List<Row> rows) throws IOException {
 
        DataFormatter dataFormatter = new DataFormatter();
        Set<String> errors = new HashSet<>();
        Map<String, Anagrafica> anagraficaMap = new HashMap<>();
        for (Row row : rows) {
            String ancodiceI = "00000"+dataFormatter.formatCellValue(row.getCell(0));
            String destinatI = dataFormatter.formatCellValue(row.getCell(1));
            
            String ancodiceD = "00000"+dataFormatter.formatCellValue(row.getCell(2));
            String destinatD = dataFormatter.formatCellValue(row.getCell(3));
            String descri = dataFormatter.formatCellValue(row.getCell(4));
            String indiri = dataFormatter.formatCellValue(row.getCell(5));
            String locali = dataFormatter.formatCellValue(row.getCell(6));
            String nazion = dataFormatter.formatCellValue(row.getCell(7));
            String testat = dataFormatter.formatCellValue(row.getCell(8));
            String quanti = dataFormatter.formatCellValue(row.getCell(9));
            String impori = dataFormatter.formatCellValue(row.getCell(10));
            String spese = dataFormatter.formatCellValue(row.getCell(11));

            try {
                Anagrafica intestatario = getAnagraficaByAncodcon(ancodiceI);
                intestatario.setDenominazione(destinatI);
                anagraficaMap.put(ancodiceI, intestatario);
                Anagrafica beneficiario = getAnagraficaByAncodcon(ancodiceD);
                beneficiario.setDenominazione(destinatD);
                anagraficaMap.put(ancodiceD, beneficiario);
            } catch (UnsupportedOperationException e) {
                errors.add(ancodiceI);
                continue;
            }
        }

        log.debug("Anagrafica ITA Estero Errori Trovati: "
                + errors.size());
        log.debug("Anagrafica ITA Estero Trovati: "
                + anagraficaMap.size());
        
        return anagraficaMap;
    }
    public Map<String,Row> getAbbonatiEstero() throws IOException {
        DataFormatter dataFormatter = new DataFormatter();
        File ac = new File(ABBONATI_ESTERO);
        Workbook wbac = new HSSFWorkbook(new FileInputStream(ac));

        Map<String, Row> rowMap = new HashMap<>();
        for (Row row : wbac.getSheetAt(0)) {
            String ancodice = dataFormatter.formatCellValue(row.getCell(0));
            if (ancodice.equalsIgnoreCase("ancodice")) {
                continue;
            }
            rowMap.put(ancodice, row);
        }
        return rowMap;
    }
    
    public Map<String,Anagrafica> importAbbonatiEstero(Map<String,Row> rowMap) throws IOException {
        DataFormatter dataFormatter = new DataFormatter();

        Set<String> errors = new HashSet<>();
        Map<String, Anagrafica> anagraficaMap = new HashMap<>();
        for (String ancodice : rowMap.keySet()) {
            Row row = rowMap.get(ancodice);
            try {
                anagraficaMap.put(ancodice, processRowAbbonatiEstero(row, ancodice, dataFormatter));
            } catch (UnsupportedOperationException e) {
                errors.add(ancodice);
                continue;
            }
        }

        log.debug("Abbonanti Estero Errori Trovati: "
                + errors.size());
        log.debug("Abbonati Estero Trovati: "
                + anagraficaMap.size());
        
        return anagraficaMap;
    }
    
    //FIXME
    public Anagrafica processRowAbbonatiEstero(Row row, String ancodice,DataFormatter dataFormatter) throws UnsupportedOperationException {
        
        String andescri = dataFormatter.formatCellValue(row.getCell(1));
        String andescr2 = dataFormatter.formatCellValue(row.getCell(2));
        String anindiri = dataFormatter.formatCellValue(row.getCell(3));
        String anindir1 = dataFormatter.formatCellValue(row.getCell(4));
        String anlocali = dataFormatter.formatCellValue(row.getCell(6));
        String annazion = dataFormatter.formatCellValue(row.getCell(8));        
        String destitolo = dataFormatter.formatCellValue(row.getCell(10));
        
        String abtestata = dataFormatter.formatCellValue(row.getCell(13));
        String tot03 = dataFormatter.formatCellValue(row.getCell(14));
        String abqtaabb = dataFormatter.formatCellValue(row.getCell(15));
        
        String abtestata1 = dataFormatter.formatCellValue(row.getCell(16));
        String tot02 = dataFormatter.formatCellValue(row.getCell(17));
        String abqtaabb1 = dataFormatter.formatCellValue(row.getCell(18));
        
        String abtestata2 = dataFormatter.formatCellValue(row.getCell(19));
        String tot01 = dataFormatter.formatCellValue(row.getCell(20));
        String abqtaabb2 = dataFormatter.formatCellValue(row.getCell(21));
        
        String abtestata3 = dataFormatter.formatCellValue(row.getCell(22));
        String tot00 = dataFormatter.formatCellValue(row.getCell(23));
        
        String abtestata4 = dataFormatter.formatCellValue(row.getCell(24));
        String tot04 = dataFormatter.formatCellValue(row.getCell(25));
        String abqtaabb4 = dataFormatter.formatCellValue(row.getCell(26));
        
        Anagrafica anagrafica = getAnagraficaByAncodcon(ancodice);
        
        populateAnagraficaEstero(anagrafica, ancodice,destitolo, andescri, andescr2, anindiri, anindir1, anlocali, annazion);
        return anagrafica;        
    }

    public Anagrafica processRowAnagrafica(Row row, String picoddio,String ancodice,DataFormatter dataFormatter) throws UnsupportedOperationException {
                    
        String andescri = dataFormatter.formatCellValue(row.getCell(2));
        String andescr2 = dataFormatter.formatCellValue(row.getCell(3));
        String anindiri = dataFormatter.formatCellValue(row.getCell(4));
        String anlocali = dataFormatter.formatCellValue(row.getCell(5));
        String anprovin = dataFormatter.formatCellValue(row.getCell(6));
        String an___cap = dataFormatter.formatCellValue(row.getCell(7));
        String annazion = dataFormatter.formatCellValue(row.getCell(8));
        String ancodfis = dataFormatter.formatCellValue(row.getCell(9));
        String anpariva = dataFormatter.formatCellValue(row.getCell(10));
        String antelefo = dataFormatter.formatCellValue(row.getCell(11));
        //String antipcon = dataFormatter.formatCellValue(row.getCell(12));
        String annumcel = dataFormatter.formatCellValue(row.getCell(13));
        String an_email = dataFormatter.formatCellValue(row.getCell(14));
        //String abcodese = dataFormatter.formatCellValue(row.getCell(15));
        
        Anagrafica anagrafica = getAnagraficaByAncodcon(ancodice);
        anagrafica.setDenominazione(andescri);
        anagrafica.setNome(andescr2);
        anagrafica.setIndirizzo(anindiri);
        anagrafica.setCitta(anlocali);
        if (!an___cap.trim().equals("")) {
            anagrafica.setCap(an___cap);
        }
        anagrafica.setProvincia(getProvincia(anprovin));
        anagrafica.setCodfis(ancodfis);
        anagrafica.setPiva(anpariva);
        anagrafica.setTelefono(antelefo);
        anagrafica.setCellulare(annumcel);
        anagrafica.setEmail(an_email);    
        anagrafica.setPaese(Paese.getBySigla(annazion));
        anagrafica.setAreaSpedizione(getAreaSpedizione(annazion));
        if (!checkDiocesi(anagrafica, 
                          annazion, 
                          ancodice, 
                          andescri, 
                          anindiri, 
                          anlocali,
                          anprovin,
                          picoddio)) {
                throw new UnsupportedOperationException();
        }

        if (anagrafica.getPaese() == Paese.ND) {
            log.warn("-----Paese non Definito------");
            log.warn(ancodice);
            log.warn(andescri);
            log.warn(anindiri);
            log.warn(anlocali);
            log.warn(anprovin);
            log.warn(annazion);
            log.warn(picoddio);
            log.warn("------------------------------");
            
            throw new UnsupportedOperationException();
        }
        
        if (annazion.equals("ITA") && anagrafica.getDiocesi() == Diocesi.DIOCESI000) {
            log.warn("-----Nazione ITA Errata ------");
            log.warn("ANCODICE: " + ancodice);
            log.warn("ANDESCRI: " + andescri);
            log.warn("ANINDIRI: " + anindiri);
            log.warn("ANLOCALI: " + anlocali);
            log.warn("ANPROVIN: " + anprovin);
            log.warn("ANNAZION: " + annazion);
            log.warn("Diocesi: "
                    + anagrafica.getDiocesi().getDetails());
            log.warn("------------------------------");
            
            throw new UnsupportedOperationException();
        }
        
        if (!checkProvincia(anagrafica, annazion, 
                          ancodice, 
                          andescri, 
                          anindiri, 
                          anlocali,
                          anprovin,
                          picoddio)) {
                throw new UnsupportedOperationException();
        }
        
        return anagrafica;
        
    }
        
    private static boolean checkDiocesi(Anagrafica anagrafica, 
            String annazion, 
            String ancodice, 
            String andescri, 
            String anindiri, 
            String anlocali,
            String anprovin,
            String picoddio) {
        if (anagrafica.getDiocesi() == Diocesi.DIOCESISTD) {
            anagrafica.setDiocesi(Diocesi.getDiocesiByCodice(picoddio));
        }
        
        if (ancodice.equals("0000062854")) {
            anagrafica.setDiocesi(Diocesi.DIOCESI193);
            log.debug("-----Diocesi 193------");
            log.debug("ANCODICE: " + ancodice);
            log.debug("Provincia: "
                    + anagrafica.getProvincia().getNome());
            log.debug("ANNAZION: " + annazion);
            log.debug("Diocesi: "
                    + anagrafica.getDiocesi().getDetails());
            log.debug("------------------------------");
            
        } else if (ancodice.equals("0000072701")) {
            anagrafica.setDiocesi(Diocesi.DIOCESI194);
            log.debug("-----Diocesi 194------");
            log.debug("ANCODICE: " + ancodice);
            log.debug("ANDESCRI: " + andescri);
            log.debug("ANINDIRI: " + anindiri);
            log.debug("ANLOCALI: " + anlocali);
            log.debug("ANPROVIN: " + anprovin);
            log.debug("ANNAZION: " + annazion);
            log.debug("Diocesi: "
                    + anagrafica.getDiocesi().getDetails());
            log.debug("------------------------------");
            
        }

        if (anagrafica.getDiocesi() == Diocesi.DIOCESISTD) {
            if (annazion.equals("RSM")) {
                anagrafica.setDiocesi(Diocesi.DIOCESI175);
                log.debug("-----Diocesi RSM------");
                log.debug("ANCODICE: " + ancodice);
                log.debug("ANDESCRI: " + andescri);
                log.debug("ANINDIRI: " + anindiri);
                log.debug("ANLOCALI: " + anlocali);
                log.debug("ANPROVIN: " + anprovin);
                log.debug("ANNAZION: " + annazion);
                log.debug("Diocesi: "
                        + anagrafica.getDiocesi().getDetails());
                log.debug("------------------------------");
                
            } else if (!annazion.equals("ITA")) {
                anagrafica.setDiocesi(Diocesi.DIOCESI000);
                log.debug("-----Diocesi ESTERO------");
                log.debug("ANCODICE: " + ancodice);
                log.debug("ANDESCRI: " + andescri);
                log.debug("ANINDIRI: " + anindiri);
                log.debug("ANLOCALI: " + anlocali);
                log.debug("ANPROVIN: " + anprovin);
                log.debug("ANNAZION: " + annazion);
                log.debug("Diocesi: "
                        + anagrafica.getDiocesi().getDetails());
                log.debug("------------------------------");
                
            } else if (anlocali.equals("ROMA")) {
                anagrafica.setDiocesi(Diocesi.DIOCESI168);
                log.debug("-----Diocesi ROMA------");
                log.debug("ANCODICE: " + ancodice);
                log.debug("ANDESCRI: " + andescri);
                log.debug("ANINDIRI: " + anindiri);
                log.debug("ANLOCALI: " + anlocali);
                log.debug("ANPROVIN: " + anprovin);
                log.debug("ANNAZION: " + annazion);
                log.debug("Diocesi: "
                        + anagrafica.getDiocesi().getDetails());
                log.debug("------------------------------");
                
            } else {
                log.warn("-----Diocesi non Definita------");
                log.warn("ANCODICE: " + ancodice);
                log.warn("ANDESCRI: " + andescri);
                log.warn("ANINDIRI: " + anindiri);
                log.warn("ANLOCALI: " + anlocali);
                log.warn("ANPROVIN: " + anprovin);
                log.warn("ANNAZION: " + annazion);
                log.warn("PICODDIO: " + picoddio);
                log.warn("------------------------------");
                
                return false;
            }
        }
        return true;
    }

    private static boolean checkProvincia( Anagrafica anagrafica, 
    String annazion, 
    String ancodice, 
    String andescri, 
    String anindiri, 
    String anlocali,
    String anprovin,
    String picoddio) {
        if (annazion.equals("ITA") && anagrafica.getProvincia() == Provincia.ND) {
            if (anlocali.equals("ROMA")) {
                anagrafica.setProvincia(Provincia.RM);
            } else if (anlocali.equals("MESSINA")) {
                anagrafica.setProvincia(Provincia.ME);
            } else if (anlocali.equals("ALESSANDRIA")) {
                anagrafica.setProvincia(Provincia.AL);
            } else if (anlocali.equals("SAVONA")) {
                anagrafica.setProvincia(Provincia.SV);
            } else if (anlocali.equals("PERUGIA")) {
                anagrafica.setProvincia(Provincia.PG);
            } else if (anlocali.equals("REGGIO CALABRIA")) {
                anagrafica.setProvincia(Provincia.RC);
            } else if (anlocali.equals("PESARO")) {
                anagrafica.setProvincia(Provincia.PU);
            } else if (anlocali.equals("PARMA")) {
                anagrafica.setProvincia(Provincia.PR);
            } else if (anlocali.equals("MANTOVA")) {
                anagrafica.setProvincia(Provincia.MN);
            } else if (anlocali.equals("TORINO")) {
                anagrafica.setProvincia(Provincia.TO);
            } else if (anlocali.equals("BOLOGNA")) {
                anagrafica.setProvincia(Provincia.BO);
            } else if (anlocali.equals("MILANO")) {
                anagrafica.setProvincia(Provincia.MI);
            } else if (anlocali.equals("NAPOLI")) {
                anagrafica.setProvincia(Provincia.NA);
            } else if (anlocali.equals("PADOVA")) {
                anagrafica.setProvincia(Provincia.PD);
            } else if (anlocali.equals("FIRENZE")) {
                anagrafica.setProvincia(Provincia.FI);
            } else if (anlocali.equals("CATANZARO")) {
                anagrafica.setProvincia(Provincia.CZ);
            } else if (anlocali.equals("GENOVA")) {
                anagrafica.setProvincia(Provincia.GE);
            } else if (anlocali.equals("LIVORNO")) {
                anagrafica.setProvincia(Provincia.LI);
            } else if (anlocali.equals("NUORO")) {
                anagrafica.setProvincia(Provincia.NU);
            } else if (anlocali.equals("MATERA")) {
                anagrafica.setProvincia(Provincia.MT);
            } else if (anlocali.equals("COSENZA")) {
                anagrafica.setProvincia(Provincia.CS);
            } else if (anlocali.equals("FERRARA")) {
                anagrafica.setProvincia(Provincia.FE);
            } else if (anlocali.equals("RAVENNA")) {
                anagrafica.setProvincia(Provincia.RA);
            } else if (anlocali.equals("TARANTO")) {
                anagrafica.setProvincia(Provincia.TA);
            } else if (anlocali.equals("TRIESTE")) {
                anagrafica.setProvincia(Provincia.TS);
            } else if (anlocali.equals("SIENA")) {
                anagrafica.setProvincia(Provincia.SI);
            } else if (anlocali.equals("BARI")) {
                anagrafica.setProvincia(Provincia.BA);
            } else if (anlocali.equals("COMO")) {
                anagrafica.setProvincia(Provincia.CO);
            } else if (anlocali.equals("PALERMO")) {
                anagrafica.setProvincia(Provincia.PA);
            } else if (anlocali.equals("VENEZIA")) {
                anagrafica.setProvincia(Provincia.VE);
            } else if (anlocali.equals("L'AQUILA")) {
                anagrafica.setProvincia(Provincia.AQ);
            } else if (anlocali.equals("CHIETI")) {
                anagrafica.setProvincia(Provincia.CH);
            } else if (anlocali.equals("PAVIA")) {
                anagrafica.setProvincia(Provincia.PV);
            } else if (anlocali.equals("SACRAMORA DI RIMINI")) {
                anagrafica.setProvincia(Provincia.RN);
                anagrafica.setCitta("RIMINI");
            } else if (anlocali.equals("GROSIO")) {
                anagrafica.setProvincia(Provincia.SO);
            } else if (anagrafica.getDiocesi() == Diocesi.DIOCESI175) {
                anagrafica.setPaese(Paese.SM);
                log.debug("-----Paese RSM------");
                log.debug("ANCODICE: " + ancodice);
                log.debug("ANDESCRI: " + andescri);
                log.debug("ANINDIRI: " + anindiri);
                log.debug("ANLOCALI: " + anlocali);
                log.debug("Paese: "
                        + anagrafica.getPaese().getNome());
                log.debug("Diocesi: "
                        + anagrafica.getDiocesi().getDetails());
                log.debug("------------------------------");
                
            } else {
                log.warn("-----Provincia non Definita------");
                log.warn("ANCODICE: " + ancodice);
                log.warn("ANDESCRI: " + andescri);
                log.warn("ANINDIRI: " + anindiri);
                log.warn("ANLOCALI: " + anlocali);
                log.warn("ANPROVIN: " + anprovin);
                log.warn("ANNAZION: " + annazion);
                log.warn("Diocesi: "
                        + anagrafica.getDiocesi().getDetails());
                log.warn("------------------------------");
                
                return false;
            }
        }
        return true;
   
    }
    public void fixAbbonatiEstero(Map<String, Anagrafica> acMap, Map<String, Anagrafica> aeMap) {
        for (String ancodice : aeMap.keySet()) {
            Anagrafica ae = aeMap.get(ancodice);
            Anagrafica ac = acMap.get(ancodice);
            ae.setDiocesi(ac.getDiocesi());
            ae.setCodfis(ac.getCodfis());
            ae.setPiva(ac.getPiva());
            ae.setTelefono(ac.getTelefono());
            ae.setCellulare(ac.getCellulare());
            ae.setEmail(ac.getEmail());    
     
            ac.setTitolo(ae.getTitolo());
        }
        
    }

    public void fixBeneficiari(Map<String, Anagrafica> acMap, Map<String, Anagrafica> eaMap,Map<String, Anagrafica> abMap) {
        for (String ancodice: abMap.keySet()) {
            if (eaMap.containsKey(ancodice)) {
                abMap.put(ancodice, eaMap.get(ancodice));
            } else if (acMap.containsKey(ancodice)) {
                abMap.put(ancodice, acMap.get(ancodice));
            } else {
                log.warn(ancodice);
            }
        }
    }

    public void fixOmaggioMessaggio(
            Map<String,Anagrafica> elencoAbbonatiMap, 
            Map<String,Anagrafica> archivioClientiMap,
            Map<String,Anagrafica> clientiOmaggio) {
        for (Anagrafica omaggioMessaggio : clientiOmaggio.values()) {
            boolean found = false;
            for (Anagrafica anagrafica: elencoAbbonatiMap.values()) {
                if (
                        anagrafica.getDenominazione().equals(omaggioMessaggio.getDenominazione())
                        && anagrafica.getProvincia() == omaggioMessaggio.getProvincia()
                        
                    ) {
                    found = true;
                    if (anagrafica.getTitolo() == TitoloAnagrafica.Nessuno) {
                        anagrafica.setTitolo(omaggioMessaggio.getTitolo());
                    }
                    omaggioMessaggio.setCodeLineBase(anagrafica.getCodeLineBase());
                    omaggioMessaggio.setDiocesi(anagrafica.getDiocesi());
                    omaggioMessaggio.setCellulare(anagrafica.getCellulare());
                    omaggioMessaggio.setTelefono(anagrafica.getTelefono());
                    omaggioMessaggio.setEmail(anagrafica.getEmail());
                    omaggioMessaggio.setCodfis(anagrafica.getCodfis());
                    omaggioMessaggio.setPiva(anagrafica.getPiva());
                    log.debug("Trovato su elencoAbbonati: {}", anagrafica);
                    break;
                }
            }
            if (found) {
                continue;
            }
            for (String ancodice : archivioClientiMap.keySet()) {
                Anagrafica anagrafica = archivioClientiMap.get(ancodice);
                if (
                        anagrafica.getDenominazione().equals(omaggioMessaggio.getDenominazione())
                        && anagrafica.getProvincia() == omaggioMessaggio.getProvincia()
                        ) {
                    found = true;
                    omaggioMessaggio.setCodeLineBase(anagrafica.getCodeLineBase());
                    if (anagrafica.getTitolo() == TitoloAnagrafica.Nessuno) {
                        anagrafica.setTitolo(omaggioMessaggio.getTitolo());
                    }
                    if (
                            omaggioMessaggio.getCodeLineBase().equals("00000000005133") 
                         || omaggioMessaggio.getCodeLineBase().equals("00000000004447")) {
                        anagrafica.setIndirizzo(omaggioMessaggio.getIndirizzo());
                        log.debug(ancodice+"---> Updated Anagrafica");
                                            }
                    if (
                            omaggioMessaggio.getCodeLineBase().equals("00000000021809") 
                        ) {
                        omaggioMessaggio.setNome("");
                        omaggioMessaggio.setTitolo(TitoloAnagrafica.VescovoAusiliare);
                        anagrafica.setTitolo(TitoloAnagrafica.VescovoAusiliare);
                        anagrafica.setIndirizzo(omaggioMessaggio.getIndirizzo());
                        log.debug(ancodice+"---> Updated Anagrafica");
                    }
                    

                    
                    omaggioMessaggio.setDiocesi(anagrafica.getDiocesi());
                    omaggioMessaggio.setCellulare(anagrafica.getCellulare());
                    omaggioMessaggio.setTelefono(anagrafica.getTelefono());
                    omaggioMessaggio.setEmail(anagrafica.getEmail());
                    omaggioMessaggio.setCodfis(anagrafica.getCodfis());
                    omaggioMessaggio.setPiva(anagrafica.getPiva());
                    log.debug("Trovato su archivio clienti: {}", anagrafica);
                    elencoAbbonatiMap.put(ancodice, omaggioMessaggio);
                    log.info(ancodice+"---> Added to Elenco Abbonati");
                    break;
                }
            }
        }
        
    }

    public void fixElencoAbbonatiCampagna(Map<String, Anagrafica> acMap, Map<String, Anagrafica> caMap) {
        int i=0;
        for (String ancodice : caMap.keySet()) {
            i++;
            Anagrafica ca = caMap.get(ancodice);
            Anagrafica ac = acMap.get(ancodice);
            
            ca.setDiocesi(ac.getDiocesi());
            ca.setCodfis(ac.getCodfis());
            ca.setPiva(ac.getPiva());
            ca.setTelefono(ac.getTelefono());
            ca.setCellulare(ac.getCellulare());
            ca.setEmail(ac.getEmail());    
     
            ac.setTitolo(ca.getTitolo());

            if (ancodice.equals("0000021498") 
                || ancodice.equals("0000012046")
                || ancodice.equals("0000069601")
                || ancodice.equals("0000003829")
                || ancodice.equals("0000074200")
                || ancodice.equals("0000068384")
                || ancodice.equals("0000013286")
                || ancodice.equals("0000022412")
                || ancodice.equals("0000011717")
                || ancodice.equals("0000070010")
                || ancodice.equals("0000067739")
                || ancodice.equals("0000067635")
                || ancodice.equals("0000017386")
                || ancodice.equals("0000012586")
                || ancodice.equals("0000018764")
                || ancodice.equals("0000072215")
                || ancodice.equals("0000012843")
                || ancodice.equals("0000023556")
                || ancodice.equals("0000005674")
                || ancodice.equals("0000073184")
                || ancodice.equals("0000068599")
                || ancodice.equals("0000062486")
                || ancodice.equals("0000012438")
                ) {
                log.debug(ancodice+"--->"+i+"--->Updated caAnagrafica");
                ca.setDenominazione(ac.getDenominazione());
                ca.setNome(ac.getNome());
            }
            
            if (ancodice.equals("0000071821") 
                    || ancodice.equals("0000026435") 
                    || ancodice.equals("0000015653")
                    || ancodice.equals("0000018136")
                    || ancodice.equals("0000006176")
                    || ancodice.equals("0000015907")
                    || ancodice.equals("0000011628")
                    || ancodice.equals("0000029845")
                    || ancodice.equals("0000005884")
                    || ancodice.equals("0000022397")
                    || ancodice.equals("0000007088")
                    || ancodice.equals("0000004835")
                    || ancodice.equals("0000059960")
                    || ancodice.equals("0000004892")
                    || ancodice.equals("0000070325")
                    || ancodice.equals("0000010067")
                    || ancodice.equals("0000004244")
                    || ancodice.equals("0000004098")
                    || ancodice.equals("0000074822")
                    || ancodice.equals("0000064058")
                    || ancodice.equals("0000072596")
                    || ancodice.equals("0000004457")
                    || ancodice.equals("0000010123")
                    || ancodice.equals("0000010104")
                    || ancodice.equals("0000007662")
                    || ancodice.equals("0000011192")
                    ) {
                log.debug(ancodice+"--->"+i+"--->Updated caAnagrafica");
                ca.setNome(ac.getNome());
                ca.setCitta(ac.getCitta());
                ca.setCap(ac.getCap());
                ca.setIndirizzo(ac.getIndirizzo());
            }            
            if (ancodice.equals("0000013374") 
                ||ancodice.equals("0000064543") 
                ||ancodice.equals("0000015427") 
                ||ancodice.equals("0000011361") 
                ||ancodice.equals("0000074822") 
                ||ancodice.equals("0000011192") 
                    ) {
                log.debug(ancodice+"--->"+i+"--->Updated caAnagrafica");
                ca.setProvincia(ac.getProvincia());
                ca.setCitta(ac.getCitta());
                ca.setIndirizzo(ac.getIndirizzo());
                ca.setCap(ac.getCap());
            }
            
            if (ancodice.equals("0000063661")
                || ancodice.equals("0000011798")
                || ancodice.equals("0000065672")
                || ancodice.equals("0000017604")
                || ancodice.equals("0000017622")
                || ancodice.equals("0000017678")
                ) {
                log.debug(ancodice+"--->"+i+"--->Updated caAnagrafica");
                ca.setCitta(ac.getCitta());
                ca.setIndirizzo(ac.getIndirizzo());
                ca.setCap(ac.getCap());
            }
            if (ancodice.equals("0000016209")
                || ancodice.equals("0000015153")
                    ) {
                log.debug(ancodice+"--->"+i+"--->Updated acAnagrafica");
                ac.setCitta(ca.getCitta());
                }

            if (ancodice.equals("0000020195")
                ||   ancodice.equals("0000069121") 
                ||   ancodice.equals("0000067234")
                    ) {
                log.debug(ancodice+"--->"+i+"--->Updated caAnagrafica");
                ca.setCap(ac.getCap());
                ca.setIndirizzo(ac.getIndirizzo());
            }
            
            if (ancodice.equals("0000020142") 
               || ancodice.equals("0000003649")
               || ancodice.equals("0000022428")
               || ancodice.equals("0000016527")
               || ancodice.equals("0000011988")
               || ancodice.equals("0000063155")
               || ancodice.equals("0000007085")
               || ancodice.equals("0000016818")
               || ancodice.equals("0000003551")
               || ancodice.equals("0000008250")
               || ancodice.equals("0000040414")
               || ancodice.equals("0000016185")
               || ancodice.equals("0000067648")
               || ancodice.equals("0000017474")
               || ancodice.equals("0000023556")
               || ancodice.equals("0000004417")
               || ancodice.equals("0000016407")
               || ancodice.equals("0000073184")
               || ancodice.equals("0000013534")
               || ancodice.equals("0000063811")
               || ancodice.equals("0000014742")
               || ancodice.equals("0000018603")
               || ancodice.equals("0000006535")
               ) {
                log.debug(ancodice+"--->"+i+"--->Updated caAnagrafica");
                ca.setIndirizzo(ac.getIndirizzo());
            }
            if (ancodice.equals("0000061880") 
            || ancodice.equals("0000066055")
            || ancodice.equals("0000005008")
            || ancodice.equals("0000012438")
            || ancodice.equals("0000006605")
            || ancodice.equals("0000020992")
               ) {
                log.debug(ancodice+"--->"+i+"--->Updated acAnagrafica");
                ac.setIndirizzo(ca.getIndirizzo());
            }
            
            if (ancodice.equals("0000069501") 
                    || ancodice.equals("0000022252")
                    || ancodice.equals("0000072596")) {
                log.debug(ancodice+"--->"+i+"--->Updated caAnagrafica");
                ac.setIndirizzoSecondaRiga(ca.getIndirizzoSecondaRiga());
            }
        }

    }
        

}
