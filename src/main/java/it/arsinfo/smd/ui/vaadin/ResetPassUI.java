package it.arsinfo.smd.ui.vaadin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;

import it.arsinfo.smd.repository.UserInfoDao;

@SpringUI(path=SmdUI.URL_RESET)
@Title("Reset Password")
public class ResetPassUI extends SmdUI {

    @Autowired
    private UserInfoDao userInfoDao;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    /**
     * 
     */
    private static final long serialVersionUID = -659806613407638574L;

    @Override
    protected void init(VaadinRequest request) {
        super.init(request,"Users");
        ResetPassEditor editor = new ResetPassEditor(userInfoDao,passwordEncoder);
        
        addSmdComponents(editor);
        
        editor.setChangeHandler(() -> {
        });
        
        editor.edit(getLoggedInUser());
        
    }

}