package it.arsinfo.smd.dao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;

import it.arsinfo.smd.data.Anno;
import it.arsinfo.smd.data.InvioSpedizione;
import it.arsinfo.smd.data.Mese;
import it.arsinfo.smd.data.SpedizioneWithItems;
import it.arsinfo.smd.data.StatoSpedizione;
import it.arsinfo.smd.data.TipoAbbonamentoRivista;
import it.arsinfo.smd.dto.AbbonamentoConRiviste;
import it.arsinfo.smd.dto.Indirizzo;
import it.arsinfo.smd.dto.SpedizioniereItem;
import it.arsinfo.smd.entity.Abbonamento;
import it.arsinfo.smd.entity.Anagrafica;
import it.arsinfo.smd.entity.Offerta;
import it.arsinfo.smd.entity.OfferteCumulate;
import it.arsinfo.smd.entity.OperazioneIncasso;
import it.arsinfo.smd.entity.Pubblicazione;
import it.arsinfo.smd.entity.RivistaAbbonamento;
import it.arsinfo.smd.entity.Spedizione;
import it.arsinfo.smd.entity.SpedizioneItem;
import it.arsinfo.smd.entity.UserInfo;
import it.arsinfo.smd.entity.Versamento;

public interface SmdService {

	void auditlog(AuditApplicationEvent auditApplicationEvent);

	void logout(String user);
	UserInfo login(String user);

    List<AbbonamentoConRiviste> get(List<Abbonamento> abbonamenti);

    void genera(Abbonamento abbonamento) throws Exception;
    void rimuovi(Abbonamento abbonamento) throws Exception;
    void sospendiSpedizioni(Abbonamento abbonamento) throws Exception;
    void riattivaSpedizioni(Abbonamento abbonamento) throws Exception;
    void aggiornaStatoStorico(Abbonamento abbonamento, int numero) throws Exception;
    void riattivaStorico(Abbonamento abbonamento) throws Exception;
    
    void rimuovi(Abbonamento abbonamento, RivistaAbbonamento rivistaAbbonamento) throws Exception;
    void aggiorna(RivistaAbbonamento rivistaAbbonamento, int numero, TipoAbbonamentoRivista tipo) throws Exception;    
    
    void generaStatisticheTipografia(Anno anno, Mese mese); 
    void generaStatisticheTipografia(Anno anno); 
    void inviaSpedizionere(Mese meseSpedizione, Anno annoSpedizione);
    

    List<SpedizioniereItem> listItems(Pubblicazione pubblicazione,Mese meseSpedizione, Anno annoSpedizione, InvioSpedizione invioSpedizione, StatoSpedizione statoSpedizione);
    List<SpedizioneWithItems> findByAbbonamento(Abbonamento abb);
        
    void incassa(Abbonamento abbonamento, Versamento versamento, UserInfo user, String description) throws Exception;    
    void storna(OperazioneIncasso operazioneIncasso,UserInfo user, String description) throws Exception;    

    void incassa(BigDecimal importo, OfferteCumulate offerte, Versamento selected, UserInfo loggedInUser, Anagrafica committente) throws Exception;
	void storna(Offerta offerta, UserInfo loggedInUser) throws Exception;
    
    SpedizioniereItem genera(SpedizioneItem spedItem);
    Indirizzo genera(Spedizione spedizione);


}
