package it.arsinfo.smd.dao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.arsinfo.smd.data.Anno;
import it.arsinfo.smd.data.TipoAbbonamentoRivista;
import it.arsinfo.smd.entity.Abbonamento;
import it.arsinfo.smd.entity.Anagrafica;
import it.arsinfo.smd.entity.RivistaAbbonamento;
import it.arsinfo.smd.entity.Pubblicazione;
import it.arsinfo.smd.entity.Storico;

public interface RivistaAbbonamentoDao extends JpaRepository<RivistaAbbonamento, Long> {

	List<RivistaAbbonamento> findByAbbonamento(Abbonamento abbonamento);
        List<RivistaAbbonamento> findByDestinatario(Anagrafica destinatario);
        List<RivistaAbbonamento> findByStorico(Storico storico);
        List<RivistaAbbonamento> findByStoricoAndAnnoInizioAndAnnoFine(Storico storico, Anno inizio, Anno fine);
        List<RivistaAbbonamento> findByPubblicazione(Pubblicazione pubblicazione);
        List<RivistaAbbonamento> findByTipoAbbonamentoRivista(TipoAbbonamentoRivista tipo);
        List<RivistaAbbonamento> findByPubblicazioneAndTipoAbbonamentoRivista(Pubblicazione pubblicazione, TipoAbbonamentoRivista t);
        void deleteByAbbonamento(Abbonamento abbonamento);
}
