package it.arsinfo.smd;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.arsinfo.smd.data.Accettazione;
import it.arsinfo.smd.data.Anno;
import it.arsinfo.smd.data.Bollettino;
import it.arsinfo.smd.data.Cassa;
import it.arsinfo.smd.data.Ccp;
import it.arsinfo.smd.data.Cuas;
import it.arsinfo.smd.data.Mese;
import it.arsinfo.smd.data.Omaggio;
import it.arsinfo.smd.data.Sostitutivo;
import it.arsinfo.smd.data.TipoPubblicazione;
import it.arsinfo.smd.entity.Abbonamento;
import it.arsinfo.smd.entity.Anagrafica;
import it.arsinfo.smd.entity.Campagna;
import it.arsinfo.smd.entity.Incasso;
import it.arsinfo.smd.entity.Operazione;
import it.arsinfo.smd.entity.Prospetto;
import it.arsinfo.smd.entity.Pubblicazione;
import it.arsinfo.smd.entity.Spedizione;
import it.arsinfo.smd.entity.Storico;
import it.arsinfo.smd.entity.Versamento;

public class Smd {

    private static final Logger log = LoggerFactory.getLogger(Smd.class);
    private static final DateFormat formatter = new SimpleDateFormat("yyMMddH");
    static final DateFormat unformatter = new SimpleDateFormat("yyMMdd");

    public static Date getStandardDate(LocalDate localDate) {
        return Smd.getStandardDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));       
    }

    public static Date getStandardDate(Date date) {
        return getStandardDate(unformatter.format(date));
    }

    public static Date getStandardDate(String yyMMdd) {
        try {
            return formatter.parse(yyMMdd+"8");
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
        return null;
    };

    public static List<Prospetto> generaProspetti(List<Pubblicazione> pubblicazione, List<Abbonamento> abbonamenti, List<Spedizione> spedizioni, Anno anno, Mese mese) {
        return null;
    }

    public static List<Operazione> generaOperazioni(
            List<Pubblicazione> pubblicazioni, 
            List<Abbonamento> abbonamenti, 
            List<Spedizione> spedizioni, 
            Anno anno,
            Set<Mese> mesi
        ) {
        List<Operazione> operazioni = new ArrayList<>();
        pubblicazioni.stream().forEach(pubblicazione -> {
            pubblicazione.getMesiPubblicazione()
                .stream()
                .filter(mese -> mesi.contains(mese))
                .map(mese -> operazioni.add(generaOperazione(pubblicazione, abbonamenti, spedizioni, anno, mese))
            );
        });
        return operazioni;
    }

    public static List<Operazione> generaOperazioni(
            Pubblicazione pubblicazione, 
            List<Abbonamento> abbonamenti, 
            List<Spedizione> spedizioni, 
            Anno anno, 
            Set<Mese> mesi) {

        return pubblicazione.getMesiPubblicazione()
                .stream()
                .filter(mese -> mesi.contains(mese))
                .map(mese -> generaOperazione(pubblicazione, abbonamenti, spedizioni, anno, mese))
                .collect(Collectors.toList());
    }
    
    public static List<Operazione> generaOperazioni(
            List<Pubblicazione> pubblicazioni, 
            List<Abbonamento> abbonamenti, 
            List<Spedizione> spedizioni, 
            Anno anno) {
        List<Operazione> operazioni = new ArrayList<>();
        pubblicazioni.stream().forEach(pubblicazione -> {
            pubblicazione.getMesiPubblicazione().stream().map(mese -> 
            operazioni.add(generaOperazione(pubblicazione, abbonamenti, spedizioni, anno, mese)
                ));
        });
        return operazioni;
    }

    public static List<Operazione> generaOperazioni(
                    Pubblicazione pubblicazione, 
                    List<Abbonamento> abbonamenti, 
                    List<Spedizione> spedizioni, 
                    Anno anno) {
        return pubblicazione.getMesiPubblicazione().stream().map(mese -> 
            generaOperazione(pubblicazione, abbonamenti, spedizioni, anno, mese)
        ).collect(Collectors.toList());
    }
    
    private static Operazione generaOperazione(
            Pubblicazione pubblicazione, 
            List<Abbonamento> abbonamenti, 
            List<Spedizione> spedizioni, 
            Anno anno, Mese mese) {
        Operazione operazione = new Operazione(pubblicazione, anno, mese);
        Integer conta = 0;
        for (Spedizione s: spedizioni) {
            if (s.getPubblicazione().getId() != pubblicazione.getId()) {
                continue;
            }
            for (Abbonamento a: abbonamenti) {
                if (s.getAbbonamento().getId() == a.getId() 
                        && a.getAnno() == anno 
                        && a.getInizio().getPosizione() <= mese.getPosizione() 
                        && a.getFine().getPosizione() >= mese.getPosizione() 
                  ) {
                        conta+=s.getNumero();
                }
            }
        }
        operazione.setStimato(conta);
        return operazione;
        
        
    }
    
    public static Versamento incassa(Incasso incasso, Versamento versamento, Abbonamento abbonamento) throws UnsupportedOperationException {
        if (incasso == null ) {
            log.error("incassa: Incasso null");
            throw new UnsupportedOperationException("incassa: Incasso null");
        }
        if (versamento == null ) {
            log.error("incassa: Versamento null");
            throw new UnsupportedOperationException("incassa: Versamento null");
        }
        if (abbonamento == null ) {
            log.error("incassa: Abbonamento null");
            throw new UnsupportedOperationException("incassa: Abbonamento null");
        }
        if (versamento.getIncasso().getId().longValue() != incasso.getId().longValue()) {
            log.error(String.format("incassa: Incasso e Versamento non sono associati. Incasso=%s, Versamento=%s",incasso.toString(),versamento.toString()));
            throw new UnsupportedOperationException("incassa: Incasso e Versamento non sono associati");               
        }
        if (abbonamento.getVersamento() != null) {
            log.error("incassa: Abbonamento e Versamento non sono associabili, abbonamento incassato");
            throw new UnsupportedOperationException("incassa: Abbonamento e Versamento non sono associabili, abbonamento incassato");
        }
        if ((versamento.getResiduo().subtract(abbonamento.getTotale()).compareTo(BigDecimal.ZERO)) < 0) {
            throw new UnsupportedOperationException("incassa: Abbonamento e Versamento non sono associabili, non rimane abbastanza credito sul versamento");            
        }
        versamento.setIncassato(versamento.getIncassato().add(abbonamento.getTotale()));
        incasso.setIncassato(incasso.getIncassato().add(abbonamento.getTotale()));
        abbonamento.setVersamento(versamento);
        return versamento;
    }

    public static Versamento dissocia(Incasso incasso, Versamento versamento, Abbonamento abbonamento) throws UnsupportedOperationException {
        if (incasso == null ) {
            log.error("dissocia: Incasso null");
            throw new UnsupportedOperationException("dissocia: Incasso null");
        }
        if (versamento == null ) {
            log.error("dissocia: Versamento null");
            throw new UnsupportedOperationException("dissocia: Versamento null");
        }
        if (abbonamento == null ) {
            log.error("dissocia: Abbonamento null");
            throw new UnsupportedOperationException("dissocia: Abbonamento null");
        }
        if (abbonamento.getVersamento() == null ) {
            log.error("dissocia: Abbonamento non incassato");
            throw new UnsupportedOperationException("dissocia: Abbonamento non incassato");
        }
        if (versamento.getIncasso().getId().longValue() != incasso.getId().longValue()) {
            log.error(String.format("dissocia: Incasso e Versamento non sono associati. Incasso=%s, Versamento=%s",incasso.toString(),versamento.toString()));
            throw new UnsupportedOperationException("incassa: Incasso e Versamento non sono associati");               
        }
        if (abbonamento.getVersamento().getId().longValue() != versamento.getId().longValue() ) {
            log.error(String.format("dissocia: Abbonamento e Versamento non sono associati. Abbonamento=%s, Versamento=%s",abbonamento.toString(),versamento.toString()));
            throw new UnsupportedOperationException("dissocia: Abbonamento e Versamento non sono associati");
        }
        versamento.setIncassato(versamento.getIncassato().subtract(abbonamento.getTotale()));
        incasso.setIncassato(incasso.getIncassato().subtract(abbonamento.getTotale()));
        abbonamento.setVersamento(null);
        return versamento;
    }

    public static String getProgressivoVersamento(int i) {
        return String.format("%09d",i);
    }
    
    public static List<Spedizione> selectSpedizioni(List<Spedizione> spedizioni, Anno anno, Mese mese, Pubblicazione pubblicazione) {
        return spedizioni.stream()
                .filter(s -> 
                    s.getPubblicazione().getId() == pubblicazione.getId() 
                    && s.getAbbonamento().getAnno() == anno
                    && pubblicazione.getMesiPubblicazione().contains(mese)
                ).collect(Collectors.toList());
    }
     
    public static boolean pagamentoRegolare(Storico storico, List<Abbonamento> abbonamenti) {
        if (storico.getOmaggio() != Omaggio.No || storico.getOmaggio() != Omaggio.ConSconto) {
            return true;
        }
        for (Abbonamento abb: abbonamenti) {
            if (abb.getIntestatario().getId() == storico.getIntestatario().getId()) {
                continue;
            }
            if (abb.getAnno() != getAnnoCorrente() || abb.getAnno() != getAnnoPassato()) {
                continue;
            }
            for (Spedizione sped: abb.getSpedizioni()) {
                if (sped.getPubblicazione().getId() != storico.getPubblicazione().getId()
                        ||
                    sped.getDestinatario().getId() != storico.getDestinatario().getId()     ) {
                    continue;
                }
                if (abb.getCosto() != BigDecimal.ZERO && sped.getOmaggio() == storico.getOmaggio() && abb.getVersamento() == null) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public static void generaCampagna(Campagna campagna, List<Storico> storici, List<Abbonamento> vecchiabb) {
        Set<Long> campagnapubblicazioniIds = campagna.getCampagnaItems().stream().map(item -> item.getPubblicazione().getId()).collect(Collectors.toSet());
        Map<Cassa,List<Storico>> cassaStorico = new HashMap<>();
        Map<Long,Anagrafica>  intestatari = new HashMap<>();
        for (Storico storico: storici) {
            if (campagna.isRinnovaSoloAbbonatiInRegola() && !pagamentoRegolare(storico, vecchiabb)) {
                continue;
            }
            if (storico.isSospeso() || !campagnapubblicazioniIds.contains(storico.getPubblicazione().getId())) {
                continue;
            }
            if (!intestatari.containsKey(storico.getIntestatario().getId())) {
                intestatari.put(storico.getIntestatario().getId(), storico.getIntestatario());
            }
            if (!cassaStorico.containsKey(storico.getCassa())) {
                cassaStorico.put(storico.getCassa(), new ArrayList<>());
            }
            cassaStorico.get(storico.getCassa()).add(storico);
        }
        List<Abbonamento> abbonamenti = new ArrayList<>();
        for (Cassa cassa: cassaStorico.keySet()) {
            Map<Long,Abbonamento> abbti = new HashMap<>();
            for (Storico storico: cassaStorico.get(cassa)) {
                if (!abbti.containsKey(storico.getIntestatario().getId())) {
                    Abbonamento abb = generateAbbonamento(storico.getIntestatario(), campagna,cassa);
                    abbti.put(storico.getIntestatario().getId(), abb);
                }
                addSpedizione(abbti.get(storico.getIntestatario().getId()),storico);
            }
            abbonamenti.addAll(abbti.values());
        }
        abbonamenti.stream().forEach(abb -> calcoloCostoAbbonamento(abb));
        campagna.setAbbonamenti(abbonamenti);
    }
    
    public static Abbonamento generateAbbonamento(Anagrafica intestatario, Campagna campagna, Cassa cassa) {
        Abbonamento abb = new Abbonamento(intestatario);
        abb.setCampagna(campagna);
        abb.setAnno(campagna.getAnno());
        abb.setInizio(campagna.getInizio());
        abb.setFine(campagna.getFine());
        abb.setCassa(cassa);
        abb.setCampo(generateCampo(campagna.getAnno(), campagna.getInizio(), campagna.getFine()));
        return abb;
    }
    
    
    public static Anno getAnnoCorrente() {
        return Anno.valueOf("ANNO"+new SimpleDateFormat("yyyy").format(new Date()));        
    }

    public static Anno getAnnoPassato() {
        Integer annoScorso = getAnnoCorrente().getAnno()-1;
        return Anno.valueOf("ANNO"+annoScorso);
    }

    public static Anno getAnnoProssimo() {
        Integer annoProssimo = getAnnoCorrente().getAnno()+1;
        return Anno.valueOf("ANNO"+annoProssimo);
    }

    public static Mese getMeseCorrente() {
        return Mese.getByCode(new SimpleDateFormat("MM").format(new Date()));        
    }

    public static Spedizione addSpedizione(Abbonamento abbonamento, Storico storico) {
        Spedizione spedizione = addSpedizione(abbonamento,storico.getPubblicazione(), storico.getDestinatario(), storico.getNumero());
        spedizione.setInvio(storico.getInvio());
        spedizione.setOmaggio(storico.getOmaggio());
        return spedizione;
    }
    
    public static Spedizione addSpedizione(Abbonamento abbonamento, 
            Pubblicazione pubblicazione,
            Anagrafica destinatario, 
            int numero) {
        if (abbonamento == null || pubblicazione == null || numero <= 0) {
            return new Spedizione();
        }
        Spedizione spedizione = new Spedizione(abbonamento, pubblicazione, destinatario, numero);
        abbonamento.addSpedizione(spedizione);
        return spedizione;
    }
    public static boolean checkCampo(String campo) {
        if (campo == null || campo.length() != 18) {
            return false;
            
        }
        
        String codice = campo.substring(0, 16);
        
        Long valorecodice = (Long.parseLong(codice) % 93);
        Integer codicecontrollo = Integer.parseInt(campo.substring(16,18));
        return codicecontrollo.intValue() == valorecodice.intValue();
    }
    
    public static boolean isVersamento(String versamento) {        
        return (
                versamento != null && versamento.length() == 200 && versamento.trim().length() == 82);
    }
    
    public static boolean isRiepilogo(String riepilogo) {
        return ( riepilogo != null &&
                 riepilogo.length() == 200 &&
                 riepilogo.trim().length() == 96 &&
                 riepilogo.substring(19,33).trim().length() == 0 &&
                 riepilogo.substring(33,36).equals("999")
                );
    }
    
    public static Incasso generateIncasso(Set<String> versamenti,
            String riepilogo) {
        final Incasso incasso = new Incasso();
        incasso.setCassa(Cassa.Ccp);
        incasso.setCuas(Cuas.getCuas(Integer.parseInt(riepilogo.substring(0,1))));
        incasso.setCcp(Ccp.getByCcp(riepilogo.substring(1,13)));
        incasso.setDataContabile(Smd.getStandardDate(riepilogo.substring(13,19)));
//          String filler = riepilogo.substring(19,33);
//          String idriepilogo = riepilogo.substring(33,36);
        incasso.setDocumenti(Integer.parseInt(riepilogo.substring(36,44)));
        incasso.setImporto(new BigDecimal(riepilogo.substring(44,54)
                + "." + riepilogo.substring(54,56)));

        incasso.setEsatti(Integer.parseInt(riepilogo.substring(56,64)));
        incasso.setImportoEsatti(new BigDecimal(riepilogo.substring(64,74)
                + "." + riepilogo.substring(74, 76)));

        incasso.setErrati(Integer.parseInt(riepilogo.substring(76,84)));
        incasso.setImportoErrati(new BigDecimal(riepilogo.substring(84,94)
                + "." + riepilogo.substring(94, 96)));

        versamenti.
            stream().
            forEach(s -> incasso.addVersamento(generateVersamento(incasso,s)));
        return incasso;
    }

    private static Versamento generateVersamento(Incasso incasso,String value)
            {
        Versamento versamento = new Versamento(incasso,new BigDecimal(value.substring(36, 44) + "." + value.substring(44, 46)));
        versamento.setBobina(value.substring(0, 3));
        versamento.setProgressivoBobina(value.substring(3, 8));
        versamento.setProgressivo(value.substring(8,15));
        versamento.setDataPagamento(Smd.getStandardDate(value.substring(27,33)));
        versamento.setBollettino(Bollettino.getTipoBollettino(Integer.parseInt(value.substring(33,36))));
        versamento.setProvincia(value.substring(46, 49));
        versamento.setUfficio(value.substring(49, 52));
        versamento.setSportello(value.substring(52, 54));
//          value.substring(54,55);
        versamento.setDataContabile(Smd.getStandardDate(value.substring(55,61)));
        versamento.setCampo(value.substring(61,79));
        versamento.setAccettazione(Accettazione.getTipoAccettazione(value.substring(79,81)));
        versamento.setSostitutivo(Sostitutivo.getTipoAccettazione(value.substring(81,82)));
        return versamento;
    }

    static int startabbonamento = 0;

    /*
     * Codice Cliente (TD 674/896) si compone di 16 caratteri numerici
     * riservati al correntista che intende utilizzare tale campo 2 caratteri
     * numerici di controcodice pari al resto della divisione dei primi 16
     * caratteri per 93 (Modulo 93. Valori possibili dei caratteri di
     * controcodice: 00 - 92)
     */
    public static String generateCampo(Anno anno, Mese inizio, Mese fine) {
        // primi 4 caratteri anno
        String campo = anno.getAnnoAsString();
        // 5 e 6 inizio
        campo += inizio.getCode();
        // 7 e 8 fine
        campo += fine.getCode();
        // 9-16
        startabbonamento++;
        campo += String.format("%08d", startabbonamento);
        campo += String.format("%02d", Long.parseLong(campo) % 93);
        return campo;
    }

    public static int getNumeroPubblicazioni(Mese inizio, Mese fine, Mese pub, TipoPubblicazione tipo) {
        int numero = 0;
        switch (tipo) {
        case ANNUALE:
            if (inizio.getPosizione() <= pub.getPosizione()
                    && fine.getPosizione() >= pub.getPosizione()) {
                numero = 1;
            }
            break;
        case SEMESTRALE:
            if (inizio.getPosizione() <= pub.getPosizione()
                    && fine.getPosizione() >= pub.getPosizione()) {
                numero += 1;
            }
            if (fine.getPosizione() >= pub.getPosizione() + 6 && inizio.getPosizione() <= pub.getPosizione() + 6) {
                numero += 1;
            }
            break;
        case MENSILE:
            numero = fine.getPosizione()
                    - inizio.getPosizione() + 1;
            break;
        case UNICO:
            numero = 1;
            break;
        default:
            break;
        }
        return numero;
    }
    
    public static void calcoloImportoIncasso(Incasso incasso) {
        BigDecimal importo = BigDecimal.ZERO;
        for (Versamento versamento: incasso.getVersamenti()) {
            importo=importo.add(versamento.getImporto());
        }
        incasso.setImporto(importo);
        incasso.setDocumenti(incasso.getVersamenti().size());
        incasso.setErrati(0);
        incasso.setEsatti(incasso.getDocumenti());
        incasso.setImportoErrati(BigDecimal.ZERO);
        incasso.setImportoEsatti(incasso.getImporto());
    }
    
    public static void calcoloCostoAbbonamento(Abbonamento abbonamento) {
        double costo = 0.0;
        Mese inizio = abbonamento.getInizio();
        Mese fine = abbonamento.getFine();
        for (Spedizione spedizione : abbonamento.getSpedizioni()) {
            costo+= generaCosto(inizio, fine, spedizione);
        }
        abbonamento.setCosto(BigDecimal.valueOf(costo));
    }
    
    public static double generaCosto(Mese inizio, Mese fine, Spedizione spedizione) { 
        return generaCosto(inizio, fine, spedizione.getPubblicazione(), spedizione.getOmaggio(), spedizione.getNumero());
    }

    public static double generaCosto(Mese inizio, Mese fine, Pubblicazione pubblicazione, Omaggio omaggio, Integer numero) {
        double costo = 0.0;
        switch (omaggio) {
        case No:
            costo = pubblicazione.getCostoUnitario().doubleValue()
                     * numero.doubleValue()
                     * getNumeroPubblicazioni(inizio,fine, pubblicazione.getMese(),pubblicazione.getTipo());
        break;
        
        case ConSconto:
            costo = pubblicazione.getCostoScontato().doubleValue()
                     * numero.doubleValue()
                     * getNumeroPubblicazioni(inizio,fine, pubblicazione.getMese(),pubblicazione.getTipo());  
            break;
            
        case CuriaDiocesiana:
            break;
        
        case Gesuiti:
            break;
            
        case CuriaGeneralizia:
            break;
            
        default:
            break;
           
        }              
        return costo;
    }

}