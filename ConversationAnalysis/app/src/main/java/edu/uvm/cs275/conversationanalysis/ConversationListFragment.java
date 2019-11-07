package edu.uvm.cs275.conversationanalysis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ConversationListFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private ConversationAdapter mAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.conversation_list, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return v;
    }

    private void updateUI() {
        ConversationLab conversationLab = ConversationLab.get(getActivity());
        List<Conversation> conversations = conversationLab.getConversations();

        mAdapter = new ConversationAdapter(conversations);
        mRecyclerView.setAdapter(mAdapter);
    }

    private class ConversationHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        private Conversation mConversation;
        private TextView mDateTextView;
        private ImageView mImageView;

        public ConversationHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.conversation_list_item, parent, false));
            itemView.setOnClickListener(this);

            mDateTextView = (TextView) itemView.findViewById(R.id.conversation_date);
            //mImageView = (ImageView) itemView.findViewById(R.id.gammatone);
        }

        public void bind(Conversation c) {
            mConversation = c;
            mDateTextView.setText(mConversation.getDate().toString()); //TODO: Format date in a normal way
        }

        @Override
        public void onClick(View view){
            //TODO: implement going to a detail view
            Toast.makeText(getActivity(),
                    mConversation.getDate() + " clicked!", Toast.LENGTH_SHORT)
                    .show();
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