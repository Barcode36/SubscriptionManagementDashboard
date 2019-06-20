package it.arsinfo.smd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import it.arsinfo.smd.data.Invio;
import it.arsinfo.smd.data.Omaggio;
import it.arsinfo.smd.entity.Abbonamento;
import it.arsinfo.smd.entity.Anagrafica;
import it.arsinfo.smd.entity.Pubblicazione;
import it.arsinfo.smd.entity.EstrattoConto;

public interface EstrattoContoDao extends JpaRepository<EstrattoConto, Long> {

	List<EstrattoConto> findByAbbonamento(Abbonamento abbonamento);
        List<EstrattoConto> findByPubblicazione(Pubblicazione pubblicazione);
        List<EstrattoConto> findByDestinatario(Anagrafica destinatario);
        List<EstrattoConto> findByDestinatarioAndPubblicazione(Anagrafica destinatario, Pubblicazione pubblicazione);
        List<EstrattoConto> findByOmaggio(Omaggio omaggio);
        List<EstrattoConto> findByInvio(Invio invio);
}
