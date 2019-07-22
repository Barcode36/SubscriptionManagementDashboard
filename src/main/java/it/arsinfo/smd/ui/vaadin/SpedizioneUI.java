package it.arsinfo.smd.ui.vaadin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;

import it.arsinfo.smd.entity.Abbonamento;
import it.arsinfo.smd.entity.Anagrafica;
import it.arsinfo.smd.repository.AbbonamentoDao;
import it.arsinfo.smd.repository.AnagraficaDao;
import it.arsinfo.smd.repository.SpedizioneDao;

@SpringUI(path = SmdUI.URL_SPEDIZIONI)
@Title("Spedizioni")
@Push
public class SpedizioneUI extends SmdUI {

    /**
     * 
     */
    private static final long serialVersionUID = 7884064928998716106L;

    @Autowired
    AnagraficaDao anagraficaDao;

    @Autowired
    SpedizioneDao spedizioneDao;
    
    @Autowired
    AbbonamentoDao abbonamentoDao;

    @Override
    protected void init(VaadinRequest request) {
        super.init(request, "Spedizioni");
        List<Anagrafica> anagrafica = anagraficaDao.findAll();
        List<Abbonamento> abbonamenti = abbonamentoDao.findAll();
        SpedizioneSearch search = new SpedizioneSearch(spedizioneDao,abbonamenti,anagrafica);
        SpedizioneGrid grid = new SpedizioneGrid("Spedizioni");
        SpedizioneEditor editor = new SpedizioneEditor(spedizioneDao, anagrafica);
        addSmdComponents(editor,search, grid);
        editor.setVisible(false);
        
        search.setChangeHandler(()-> {
            grid.populate(search.find());
        });
        
        grid.setChangeHandler(() -> {
            if (grid.getSelected() == null) {
                return;
            }
            editor.edit(grid.getSelected());
            setHeader(grid.getSelected().getHeader());
            hideMenu();
            search.setVisible(false);
        });

        editor.setChangeHandler(() -> {
            grid.populate(search.find());
            showMenu();
            search.setVisible(true);
            setHeader("Estratto Conto");
            editor.setVisible(false);
        });

        grid.populate(search.find());

    }
}