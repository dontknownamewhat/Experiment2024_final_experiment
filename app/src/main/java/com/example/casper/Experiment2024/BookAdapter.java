package com.example.casper.Experiment2024;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
    private List<Book> bookList;

    public BookAdapter(List<Book> bookList) {
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public void addBook(int position, Book book) {
        bookList.add(position + 1, book);
        notifyItemInserted(position + 1);
    }

    public void updateBook(int position, Book book) {
        bookList.set(position, book);
        notifyItemChanged(position);
    }

    public void removeBook(int position) {
        bookList.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 可以在这里找到视图的子项并赋值给成员变量
        }

        public void bind(Book book) {
            // 绑定Book对象的数据到视图组件，例如标题、价格等
        }
    }
}