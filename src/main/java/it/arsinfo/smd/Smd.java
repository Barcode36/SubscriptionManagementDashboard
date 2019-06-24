package it.arsinfo.smd;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import it.arsinfo.smd.data.Accettazione;
import it.arsinfo.smd.data.Anno;
import it.arsinfo.smd.data.Bollettino;
import it.arsinfo.smd.data.Cassa;
import it.arsinfo.smd.data.Ccp;
import it.arsinfo.smd.data.Cuas;
import it.arsinfo.smd.data.Invio;
import it.arsinfo.smd.data.InvioSpedizione;
import it.arsinfo.smd.data.Mese;
import it.arsinfo.smd.data.TipoEstrattoConto;
import it.arsinfo.smd.data.Sostitutivo;
import it.arsinfo.smd.data.StatoAbbonamento;
import it.arsinfo.smd.data.Stato;
import it.arsinfo.smd.data.TipoPubblicazione;
import it.arsinfo.smd.entity.Abbonamento;
import it.arsinfo.smd.entity.Anagrafica;
import it.arsinfo.smd.entity.Campagna;
import it.arsinfo.smd.entity.CampagnaItem;
import it.arsinfo.smd.entity.Incasso;
import it.arsinfo.smd.entity.Operazione;
import it.arsinfo.smd.entity.Pubblicazione;
import it.arsinfo.smd.entity.Spedizione;
import it.arsinfo.smd.entity.EstrattoConto;
import it.arsinfo.smd.entity.Storico;
import it.arsinfo.smd.entity.Versamento;

@Configuration
public class Smd {

    @Bean
    public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
    }

    private static final Logger log = LoggerFactory.getLogger(Smd.class);
    private static final DateFormat formatter = new SimpleDateFormat("yyMMddH");
    static final DateFormat unformatter = new SimpleDateFormat("yyMMdd");    
    public static String decodeForGrid(boolean status) {
        if (status) {
            return "si";
        }
        return "no";
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
    public static String getProgressivoVersamento(int i) {
        return String.format("%09d",i);
    }

    public static boolean spedizionePosticipata(Spedizione spedizione, int anticipoSpedizione) {
        if (spedizione.getAnnoPubblicazione() == spedizione.getAnnoSpedizione()) {
            return spedizione.getMesePubblicazione().getPosizione() - spedizione.getMeseSpedizione().getPosizione() == anticipoSpedizione;
        }
        
        if (spedizione.getAnnoPubblicazione().getAnno() > spedizione.getAnnoSpedizione().getAnno()) {
            return 12 - spedizione.getMeseSpedizione().getPosizione() + spedizione.getMesePubblicazione().getPosizione() == anticipoSpedizione;
        }
        return false;
    }
    
    //FIXME 
    public static EstrattoConto creaEstrattoConto(
            Abbonamento abb, 
            Pubblicazione p, 
            Anagrafica destinatario, 
            TipoEstrattoConto tipoEstrattoConto,
            Invio invio,
            InvioSpedizione invioSpedizione,
            Integer numero,
            Mese meseinizio,
            Anno annoinizio,
            Mese mesefine,
            Anno annofine
            ) {
        EstrattoConto ec = new EstrattoConto();
        ec.setAbbonamento(abb);
        ec.setPubblicazione(p);
        ec.setDestinatario(destinatario);
        ec.setTipoEstrattoConto(tipoEstrattoConto);
        ec.setNumero(numero);
        ec.setInvio(invio);
        if (annoinizio == annofine) {
            
        }
        EnumSet.allOf(Anno.class)
        .stream()
        .filter(anno -> anno.getAnno() >= annoinizio.getAnno() && anno.getAnno() <= annofine.getAnno())
        .forEach(anno -> {
            creaSpedizione(mesePubblicazione, annoPubblicazione, anticipoSpedizione);
            
        });
        
        return ec;
    }
    
    //FIXME crea spedizione condizionato dal mese di spedizione
    public static Spedizione creaSpedizione(Mese mesePubblicazione, Anno annoPubblicazione, int anticipoSpedizione) {
        Spedizione spedizione = new Spedizione();
        spedizione.setMesePubblicazione(mesePubblicazione);
        spedizione.setAnnoPubblicazione(annoPubblicazione);
        if (mesePubblicazione.getPosizione()-anticipoSpedizione <= 0) {
            spedizione.setMeseSpedizione(Mese.getByPosizione(12-mesePubblicazione.getPosizione()-anticipoSpedizione));
            spedizione.setAnnoSpedizione(Anno.getAnnoPrecedente(annoPubblicazione));
        } else {
            spedizione.setMeseSpedizione(Mese.getByPosizione(mesePubblicazione.getPosizione()-anticipoSpedizione));
            spedizione.setAnnoSpedizione(annoPubblicazione);
        }
        return spedizione;
    }
    public static Campagna generaCampagna(final Campagna campagna, List<Anagrafica> anagrafiche, List<Storico> storici, List<Pubblicazione> pubblicazioni) {
        final Map<Long,Pubblicazione> campagnapubblicazioniIds = new HashMap<>();
        pubblicazioni.stream().forEach(p -> {
            CampagnaItem ci = new CampagnaItem();
            ci.setCampagna(campagna);
            ci.setPubblicazione(p);
            campagna.addCampagnaItem(ci);
            campagnapubblicazioniIds.put(p.getId(),p);            
        });

        final List<Abbonamento> abbonamenti = new ArrayList<>();
        anagrafiche.stream().forEach(a -> {
            final Map<Cassa,List<Storico>> cassaStorico = new HashMap<>();
            storici.stream()
            .filter(
                storico -> 
                (storico.getIntestatario().getId() == a.getId() 
                   && campagnapubblicazioniIds.containsKey(storico.getPubblicazione().getId()) 
                   && storico.attivo())
            )
            .forEach(storico -> { 
                if (!cassaStorico.containsKey(storico.getCassa())) {
                    cassaStorico.put(storico.getCassa(), new ArrayList<>());
                }
                cassaStorico.get(storico.getCassa()).add(storico);
            });
            for (Cassa cassa: cassaStorico.keySet()) {
                Abbonamento abbonamento = new Abbonamento();
                abbonamento.setIntestatario(a);
                abbonamento.setCampagna(campagna);
                abbonamento.setAnno(campagna.getAnno());
                abbonamento.setCassa(cassa);
                abbonamento.setCampo(generaVCampo(abbonamento.getAnno()));
                abbonamento.setStatoAbbonamento(StatoAbbonamento.PROPOSTA);
                for (Storico storico: cassaStorico.get(cassa)) {
                    Pubblicazione pubblicazione = 
                            campagnapubblicazioniIds.get(storico.getPubblicazione().getId());
                    final EstrattoConto estrattoConto = new EstrattoConto();
                    estrattoConto.setStorico(storico);
                    estrattoConto.setAbbonamento(abbonamento);
                    estrattoConto.setPubblicazione(pubblicazione);
                    estrattoConto.setNumero(storico.getNumero());
                    estrattoConto.setTipoEstrattoConto(storico.getTipoEstrattoConto());
                    estrattoConto.setDestinatario(storico.getDestinatario());
                    estrattoConto.setInvio(storico.getInvio());
                    storico.getPubblicazione().getMesiPubblicazione().forEach( mese -> {
                        Spedizione spedizione = Smd.creaSpedizione(mese,campagna.getAnno(), pubblicazione.getAnticipoSpedizione());
                        spedizione.setEstrattoConto(estrattoConto);
                        spedizione.setInvioSpedizione(storico.getInvioSpedizione());
                        estrattoConto.addSpedizione(spedizione);
                    });
                    calcoloImportoEC(estrattoConto);
                    abbonamento.addEstrattoConto(estrattoConto);
                }
                abbonamenti.add(abbonamento);
            }
            
        });        
        campagna.setAbbonamenti(abbonamenti);
        return campagna;
    }

    public static void calcoloImportoEC(EstrattoConto ec) throws UnsupportedOperationException {
        double costo=0.0;
        double spesePostali = 0.0;
        switch (ec.getTipoEstrattoConto()) {
        case Ordinario:
            spesePostali = ec.getPubblicazione().getSpeseSpedizione().doubleValue() * ec.getNumeroSpedizioniConSpesePostali();
            costo = ec.getPubblicazione().getAbbonamentoItalia().doubleValue() * ec.getNumero().doubleValue();
            if (!ec.hasAllMesiPubblicazione()) {
              
             costo = ec.getPubblicazione().getCostoUnitario().doubleValue()
                     * ec.getNumero().doubleValue()
                     * Double.valueOf(ec.getSpedizioni().size());
            }
            break;

        case Web:
            if (!ec.hasAllMesiPubblicazione()) {
                    throw new UnsupportedOperationException("Valori mesi inizio e fine non ammissibili per " + TipoEstrattoConto.Web);
            }
            costo = ec.getPubblicazione().getAbbonamentoWeb().doubleValue()
                    * ec.getNumero().doubleValue();  
            break;

        case Scontato:
            if (!ec.hasAllMesiPubblicazione()) {
                throw new UnsupportedOperationException("Valori mesi inizio e fine non ammissibili per " + TipoEstrattoConto.Web);
            }
            costo = ec.getPubblicazione().getAbbonamentoConSconto().doubleValue()
                * ec.getNumero().doubleValue();  
            break;

        case Sostenitore:
            if (!ec.hasAllMesiPubblicazione()) {
                throw new UnsupportedOperationException("Valori mesi inizio e fine non ammissibili per " + TipoEstrattoConto.Web);
            }
            costo = ec.getPubblicazione().getAbbonamentoSostenitore().doubleValue()
                     * ec.getNumero().doubleValue();  
            break;
                
        case EuropaBacinoMediterraneo:
            if (!ec.hasAllMesiPubblicazione()) {
                throw new UnsupportedOperationException("Valori mesi inizio e fine non ammissibili per " + TipoEstrattoConto.Web);
            }
            costo = ec.getPubblicazione().getAbbonamentoEuropa().doubleValue()
                     * ec.getNumero().doubleValue();  
            break;
                
        case AmericaAfricaAsia:
            if (!ec.hasAllMesiPubblicazione()) {
                throw new UnsupportedOperationException("Valori mesi inizio e fine non ammissibili per " + TipoEstrattoConto.Web);
            }
            costo = ec.getPubblicazione().getAbbonamentoAmericaAsiaAfrica().doubleValue()
                     * ec.getNumero().doubleValue();  
            break;
        case OmaggioCuriaDiocesiana:
            break;
        case OmaggioCuriaGeneralizia:
            break;
        case OmaggioDirettoreAdp:
            break;
        case OmaggioEditore:
            break;
        case OmaggioGesuiti:
            break;
        default:
            break;

        }          
        ec.setSpesePostali(BigDecimal.valueOf(spesePostali));
        ec.setImporto(BigDecimal.valueOf(costo));
    }

    public static List<EstrattoConto> estrattiContoDaAggiornare(List<EstrattoConto> estrattiConto) {
        return estrattiConto
                .stream()
                .filter(
                    s -> s.getAbbonamento().getAnno().getAnno() == getAnnoCorrente().getAnno() && s.getStatoEstrattoConto() != getSospendiSpedizione(s)
                    ).collect(Collectors.toList());
    }
    
    public static Stato getSospendiSpedizione(EstrattoConto estrattoConto) {

        Stato sospendiSpedizione=estrattoConto.getStatoEstrattoConto();
        switch (estrattoConto.getTipoEstrattoConto()) {
        case Ordinario:
            sospendiSpedizione = sospendiSpedizione(estrattoConto);
            break;

        case Scontato:    
            sospendiSpedizione = sospendiSpedizione(estrattoConto);
            break;
        case OmaggioCuriaDiocesiana:
            break;
        case OmaggioCuriaGeneralizia:
            break;
        case OmaggioGesuiti:
            break;
        default:
            break;
        }
        return sospendiSpedizione;
    }

    public static Stato getStatoStorico(Storico storico, List<Abbonamento> abbonamenti) {
        Stato pagamentoRegolare = Stato.VALIDO;
        switch (storico.getTipoEstrattoConto()) {
        case Ordinario:
            pagamentoRegolare = checkVersamento(storico, abbonamenti);
            break;
        case Scontato:    
            pagamentoRegolare = checkVersamento(storico, abbonamenti);
            break;
        default:
            pagamentoRegolare = checkVersamento(storico, abbonamenti);
            break;
        }
        return pagamentoRegolare;
    }
    
    private static boolean sospendiEstrattoConto(EstrattoConto estrattoConto) {
        Abbonamento abbonamento = estrattoConto.getAbbonamento();
            if (abbonamento.getTotale().signum() > 0 &&  abbonamento.getVersamento() == null) {
                return true;
            }
            return false;
    }

    private static Stato checkVersamento(Storico storico, List<Abbonamento> abbonamenti) {
        for (Abbonamento abb: abbonamenti) {
            if (abb.getIntestatario().getId() != storico.getIntestatario().getId() 
                    || abb.getCampagna() == null
                    || abb.getAnno().getAnno() != getAnnoCorrente().getAnno()) {
                continue;
            }
            for (EstrattoConto sped: abb.getEstrattiConto()) {
                if (sped.getStorico().getId() != storico.getId()) {
                    continue;
                }
                if (abb.getTotale().signum() == 0 ) {
                    return Stato.VALIDO;
                }
                if (abb.getTotale().signum() > 0 &&  abb.getVersamento() == null) {
                    return Stato.SOSPESO;
                }
                if (abb.getTotale().signum() > 0 &&  abb.getVersamento() != null) {
                    return Stato.VALIDO;
                }
            }
        }
        return Stato.SOSPESO;
    }
        
    /*
     * Codice Cliente (TD 674/896) si compone di 16 caratteri numerici
     * riservati al correntista che intende utilizzare tale campo 2 caratteri
     * numerici di controcodice pari al resto della divisione dei primi 16
     * caratteri per 93 (Modulo 93. Valori possibili dei caratteri di
     * controcodice: 00 - 92)
     */
    public static String generaVCampo(Anno anno) {
        // primi 2 caratteri anno
        String campo = anno.getAnnoAsString().substring(2, 4);
        // 3-16
        campo += String.format("%014d", ThreadLocalRandom.current().nextLong(99999999999999l));
        campo += String.format("%02d", Long.parseLong(campo) % 93);
        return campo;
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

    public static int calcolaNumeroPubblicazioni(Mese inizio, Mese fine, EnumSet<Mese> pub, TipoPubblicazione tipo) {
        int numero = 0;
        switch (tipo) {
        case ANNUALE:
            if (pub.isEmpty() || pub.size() != 1) {
                break;
            }
            if (inizio.getPosizione() <= pub.iterator().next().getPosizione()
                    && fine.getPosizione() >= pub.iterator().next().getPosizione()) {
                numero = 1;
            }
            break;
        case SEMESTRALE:
            if (pub.isEmpty() || pub.size() != 2) {
                break;
            }
            Iterator<Mese> iter = pub.iterator();
            Mese pp = iter.next();
            Mese sp = iter.next();
            if (inizio.getPosizione() <= pp.getPosizione()
                    && fine.getPosizione() >= pp.getPosizione()) {
                numero += 1;
            }
            if (fine.getPosizione() >= sp.getPosizione() && inizio.getPosizione() <= sp.getPosizione()) {
                numero += 1;
            }
            break;
        case MENSILE:
            for (int i=inizio.getPosizione(); i <= fine.getPosizione();i++) {
                if (pub.contains(Mese.getByPosizione(i))) {
                    numero++;
                }
            }
            break;
        case UNICO:
            numero = 1;
            break;
        default:
            break;
        }
        return numero;
    }

    public static Date getStandardDate(LocalDate localDate) {
        return getStandardDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));       
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

    public static List<Operazione> generaOperazioni(
            List<Pubblicazione> pubblicazioni, 
            List<Abbonamento> abbonamenti
        ) {
        Anno anno = getAnnoCorrente();
        Mese mese = getMeseCorrente();
        List<Operazione> operazioni = new ArrayList<>();
        pubblicazioni.stream().forEach(p -> {
            Operazione operazione = generaOperazione(p, abbonamenti,mese,anno);
            if (operazione.getStimato() != 0) {
                    operazioni.add(operazione);
            }
        }
        );
        return operazioni;
    }

    public static Operazione generaOperazione(
            Pubblicazione pubblicazione, 
            List<Abbonamento> abbonamenti, Mese mese, Anno anno) {
        final Operazione op = new Operazione(pubblicazione, anno, mese);
        op.setStimato(0);
        abbonamenti.stream()
            .filter(a ->a.getAnno() == anno)
            .forEach( a -> a.getEstrattiConto().stream()
              .filter(ec -> ec.getPubblicazione().getId() == pubblicazione.getId())
              .forEach(ec -> ec.getSpedizioni().stream()
                  .filter( s ->!s.isSospesa() && s.getMeseSpedizione() == mese && s.getAnnoSpedizione() == anno)
                      .forEach( s -> 
                          {
                              op.setStimato(op.getStimato()+ec.getNumero());
                              switch (s.getInvioSpedizione()) {
                              case  Spedizioniere: 
                                  op.setSped(op.getSped()+ec.getNumero());
                                  break;
                              case AdpSede:
                                  op.setSede(op.getSede()+ec.getNumero());
                                  break;
                              default:
                                break;
                              }
                         }
                          )
                  )
             );
                        
        return op;        
    }

    public static List<Spedizione> generaSpedizioni(List<Abbonamento> abbonamenti, InvioSpedizione invioSpedizione) {
        final List<Spedizione> spedizioni = new ArrayList<>();
        Anno anno = getAnnoCorrente();
        Mese mese = getMeseCorrente();
        abbonamenti.stream().filter(a ->a.getAnno() == anno)
            .forEach(a -> {
                a.getEstrattiConto().stream()
                .forEach( ec -> {
                    spedizioni.addAll(ec.getSpedizioni().stream().filter(s -> 
                                !s.isSospesa() && s.getInvioSpedizione() == invioSpedizione
                                && s.getMeseSpedizione() == mese
                                && s.getAnnoSpedizione() == anno
                            ).collect(Collectors.toList()));
                });
        });
        return spedizioni;
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
    
    public static Incasso generaIncasso(Set<String> versamenti,
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
    public static void calcoloAbbonamento(Abbonamento abb) {
        abb.getEstrattiConto().forEach(ec -> calcoloImportoEC(ec));
    }
}
