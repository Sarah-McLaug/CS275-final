package edu.uvm.cs275.conversationanalysis;

import androidx.fragment.app.Fragment;

public class ConversationListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new ConversationListFragment();
    }

}
