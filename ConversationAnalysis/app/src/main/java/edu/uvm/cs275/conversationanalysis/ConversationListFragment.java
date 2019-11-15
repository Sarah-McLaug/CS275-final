package edu.uvm.cs275.conversationanalysis;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConversationListFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private ConversationAdapter mAdapter;
    private BottomNavigationView mNavMenu;
    
    // This method opens the respective activity upon navigation button press.
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        switch ((item.getItemId())) {
            case R.id.nav_record:
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_view:
                // do nothing because we're already on that activity.
                break;
        }
        return true;
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.conversation_list, container, false);

        mRecyclerView = v.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mNavMenu = v.findViewById(R.id.bottom_navigation);
        mNavMenu.setOnNavigationItemSelectedListener(navListener);

        updateUI();

        return v;
    }

    private void updateUI() {
        ConversationManager conversationLab = ConversationManager.getInstance(getActivity());
        List<Conversation> conversations = conversationLab.getConversations();
        conversations.sort(Comparator.comparing(Conversation::getDate));
        Collections.reverse(conversations);

        mAdapter = new ConversationAdapter(conversations);
        mRecyclerView.setAdapter(mAdapter);
    }

    private class ConversationHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private Conversation mConversation;
        private TextView mDateTextView;
        private ImageView mImageView;

        public ConversationHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.conversation_list_item, parent, false));
            itemView.setOnClickListener(this);

            mDateTextView = itemView.findViewById(R.id.conversation_date);
            mImageView = itemView.findViewById(R.id.gammatone);
        }

        public void bind(Conversation c) {
            mConversation = c;
            mDateTextView.setText(mConversation.getDate().toString()); //TODO: Format date in a normal way

            Path imagePath = mConversation.getImageFile(getContext());
            File image = imagePath.toFile();

            Bitmap bmp = BitmapFactory.decodeFile(image.getAbsolutePath());
            mImageView.setImageBitmap(bmp);
        }

        @Override
        public void onClick(View view) {
            Intent intent = DetailView.newIntent(getActivity(), mConversation);
            startActivity(intent);
        }
    }

    private class ConversationAdapter extends RecyclerView.Adapter<ConversationHolder> {
        private List<Conversation> mConversations;

        public ConversationAdapter(List<Conversation> conversations) {
            mConversations = conversations;
        }

        @Override
        public ConversationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new ConversationHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(ConversationHolder holder, int position) {
            Conversation conversation = mConversations.get(position);
            holder.bind(conversation);
        }

        @Override
        public int getItemCount() {
            return mConversations.size();
        }
    }
}
