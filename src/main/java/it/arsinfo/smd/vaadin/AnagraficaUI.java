package it.arsinfo.smd.vaadin;


import it.arsinfo.smd.entity.Anagrafica;
import it.arsinfo.smd.entity.Anagrafica.Diocesi;
import it.arsinfo.smd.repository.AnagraficaDao;

import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.vaadin.annotations.Title;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SpringUI(path="anagrafica")
@Title("Anagrafica Clienti ADP")
public class AnagraficaUI extends UI {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7884064928998716106L;

	Grid<Anagrafica> grid;
	@Autowired
	AnagraficaDao repo;
	
	@Override
	protected void init(VaadinRequest request) {
		Assert.notNull(repo, "repo must be not null");
		Label header = new Label("Anagrafica Clienti");
		Button addNewBtn = new Button("Aggiungi ad Anagrafica", VaadinIcons.PLUS);		
		TextField filterCognome = new TextField();
		ComboBox<Anagrafica.Diocesi> filterDiocesi = new ComboBox<Anagrafica.Diocesi>(null,EnumSet.allOf(Anagrafica.Diocesi.class));
		
		grid = new Grid<>(Anagrafica.class);
		AnagraficaEditor editor = new AnagraficaEditor(repo);
		HorizontalLayout actions = new HorizontalLayout(filterDiocesi,filterCognome,addNewBtn);
		VerticalLayout layout = new VerticalLayout();
		layout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		layout.addComponents(header,editor,actions,grid);
		setContent(layout);

		header.addStyleName(ValoTheme.LABEL_H2);
		
		filterDiocesi.setEmptySelectionAllowed(false);
		filterDiocesi.setPlaceholder("Cerca per Diocesi");
		
		filterCognome.setPlaceholder("Cerca per Cognome");
		
		grid.setColumns("id", "nome", "cognome","diocesi","indirizzo");		
		grid.getColumn("id").setMaximumWidth(50);
		grid.setWidth("80%");

		editor.setWidth("80%");

		filterDiocesi.addSelectionListener(e-> listCustomer(e.getSelectedItem().get()));

		filterCognome.setValueChangeMode(ValueChangeMode.EAGER);
		filterCognome.addValueChangeListener(e -> listCustomers(e.getValue()));		

		grid.asSingleSelect().addValueChangeListener(e -> {
			editor.editCustomer(e.getValue());
		});
		
		addNewBtn.addClickListener(e -> editor.editCustomer(new Anagrafica("", "")));

		editor.setChangeHandler(() -> {
			editor.setVisible(false);
			listCustomers(filterCognome.getValue());
		});
		listCustomers(null);

	}

	void listCustomers(String filterText) {
		if (StringUtils.isEmpty(filterText)) {
			grid.setItems(repo.findAll());
		}
		else {
			grid.setItems(repo.findByCognomeStartsWithIgnoreCase(filterText));
		}
	}
	
	void listCustomer(Diocesi diocesi) {
		if (diocesi != null ) {
			grid.setItems(repo.findByDiocesi(diocesi));
		} else {
			grid.setItems(repo.findAll());
		}
	}

}