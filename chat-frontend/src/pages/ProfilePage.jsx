import { useForm } from "react-hook-form";
import toast from "react-hot-toast";
import { updateProfile } from "../services/userService";
import { useAuth } from "../context/AuthContext";

export default function ProfilePage() {
  const { user } = useAuth();
  const { register, handleSubmit, formState: { isSubmitting } } = useForm({
    defaultValues: { fullName: user?.fullName, bio: "", avatarUrl: "" },
  });

  const onSubmit = async (data) => {
    try {
      await updateProfile(data);
      toast.success("Profile updated");
    } catch {
      toast.error("Update failed");
    }
  };

  return (
    <div className="max-w-md mx-auto mt-10">
      <h1 className="text-xl font-bold mb-4">Edit profile</h1>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-3">
        <input {...register("fullName")} placeholder="Full name" className="w-full border rounded-lg px-3 py-2" />
        <textarea {...register("bio")} placeholder="Bio" className="w-full border rounded-lg px-3 py-2" />
        <input {...register("avatarUrl")} placeholder="Avatar URL" className="w-full border rounded-lg px-3 py-2" />
        <button disabled={isSubmitting} className="bg-blue-600 text-white px-4 py-2 rounded-lg">
          Save
        </button>
      </form>
    </div>
  );
}
