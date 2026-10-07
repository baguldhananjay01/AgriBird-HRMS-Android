package com.agribird.hrmsapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agribird.hrmsapp.R;

public class EditProfileAdapter extends RecyclerView.Adapter<EditProfileAdapter.ViewHolder> {

    private final String initialName;
    private final String initialEmail;
    private final String initialPhone;
    private final String initialEmergency;
    private final String initialAddress;
    private final OnProfileSaveListener saveListener;
    private final View.OnClickListener cancelListener;


    public interface OnProfileSaveListener {
        void onSave(String name, String email, String phone, String emergency, String address);
    }


    public EditProfileAdapter(String initialName, String initialEmail, String initialPhone,
                              String initialEmergency, String initialAddress,
                              OnProfileSaveListener saveListener, View.OnClickListener cancelListener) {
        this.initialName = initialName;
        this.initialEmail = initialEmail;
        this.initialPhone = initialPhone;
        this.initialEmergency = initialEmergency;
        this.initialAddress = initialAddress;
        this.saveListener = saveListener;
        this.cancelListener = cancelListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_edit_profile_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.editName.setText(initialName);
        holder.editEmail.setText(initialEmail);
        holder.editPhone.setText(initialPhone);
        holder.editEmergencyPhone.setText(initialEmergency);
        holder.editAddress.setText(initialAddress);


        holder.btnSave.setOnClickListener(v -> {
            String name = holder.editName.getText().toString().trim();
            String email = holder.editEmail.getText().toString().trim();
            String phone = holder.editPhone.getText().toString().trim();
            String emergency = holder.editEmergencyPhone.getText().toString().trim();
            String address = holder.editAddress.getText().toString().trim();

            if (saveListener != null) {
                saveListener.onSave(name, email, phone, emergency, address);
            }
        });


        holder.btnCancel.setOnClickListener(cancelListener);
    }

    @Override
    public int getItemCount() {
        return 1;

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        EditText editName, editEmail, editPhone, editEmergencyPhone, editAddress;
        Button btnSave, btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            editName = itemView.findViewById(R.id.editName);
            editEmail = itemView.findViewById(R.id.editEmail);
            editPhone = itemView.findViewById(R.id.editPhone);
            editEmergencyPhone = itemView.findViewById(R.id.editEmergencyPhone);
            editAddress = itemView.findViewById(R.id.editAddress);
            btnSave = itemView.findViewById(R.id.btnSave);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}